package keth.tools.mx

import com.sun.management.OperatingSystemMXBean
import io.vertx.core.http.ServerWebSocket
import keth.tools.Constants
import keth.tools.ResourceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Files

import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.streams.asSequence

data class MachineInfoDTO(val cpuUsage: Double, val memoryFree: Long, val memoryTotal: Long, val dbSize: Long, val freeSpace: Long)


abstract class MxData {

    protected val logger = LoggerFactory.getLogger(this.javaClass.name)

    protected val bean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    protected val properties = Properties()
    protected val DB_SIZE_CACHE_EVICT_MS = 60 * 1000;    protected var dbSizeMeasurementTime = 0L; protected var dbSize = 0L;
    protected lateinit var dbDir :File

    var mxInfoChannel = Channel<MachineInfoDTO>()


    init {

        val propsFile = ResourceUtils.getResource("config.properties" )
        val inputStream = FileInputStream(propsFile.getFile())
        properties.load(inputStream)
        dbDir = File(Constants.chainBasePath.toUri())
    }

    fun CoroutineScope.broadCastMXInfo(channel: ReceiveChannel<MachineInfoDTO>) = launch {

        for (node in channel) {
            mxInfoChannel.send(node)
        }

    }


}

object MxSource : MxData() {


    fun broadCastMxInfo() = GlobalScope.launch {
        broadCastMXInfo(produceMxInfo())
    }


    fun CoroutineScope.produceMxInfo() = produce<MachineInfoDTO> {

        while (true) {

            send(MachineInfoDTO(bean.systemCpuLoad * 100, Runtime.getRuntime().freeMemory(),
                    Runtime.getRuntime().maxMemory(), getDbDirSize(), getFreeDiskSpace(dbDir)))
            delay(3000L)
        }
    }

    suspend fun getDbDirSize( ): Long {
        if ((System.currentTimeMillis() - dbSizeMeasurementTime) < DB_SIZE_CACHE_EVICT_MS) {
            return dbSize;
        }

        this.dbSizeMeasurementTime = System.currentTimeMillis();

        try {

            val stream = Files.walk(Constants.chainBasePath)

            // https://stackoverflow.com/questions/34642254/what-java-8-stream-collect-equivalents-are-available-in-the-standard-kotlin-libr
            // asSequence == lazy so better performance
            dbSize = stream.asSequence().filter {  p -> p.toFile().isFile()  }.map { it.toFile().length() }.sum()

        } catch (e: IOException) {
            logger.error("Unable to calculate db size", e)
        }

        return dbSize;
    }

    suspend fun getFreeDiskSpace(dbDir: File): Long {
        return dbDir.usableSpace;

    }

}



fun main(args: Array<String>) {

    val latch =  CountDownLatch(1);

    GlobalScope.launch {
        val brodcastJob = MxSource.broadCastMxInfo()
        delay(5000)
        processMachineInfo(MxSource.mxInfoChannel )
    }

    latch.await();

}


fun CoroutineScope.processMachineInfo(inStream: Channel<MachineInfoDTO>)  = launch {

    for (node in inStream) {
        println(node)
    }

}
