package keth.tools.client.mx


import com.sun.management.OperatingSystemMXBean
import keth.tools.client.PropertyValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Files
import kotlin.streams.asSequence


data class MachineInfoDTO(val cpuUsage: Double, val memoryFree: Long, val memoryTotal: Long, val dbSize: Long, val freeSpace: Long)


@Component
class MxDataProvider(@Autowired private val props: PropertyValues) {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private val bean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    private val DB_SIZE_CACHE_EVICT_MS = 60 * 1000;

    private var dbDir = File(props.dbDir)
    private var dbSizeMeasurementTime = 0L;
    private var dbSize = 0L;

    var mxInfoChannel = Channel<MachineInfoDTO>()

    fun broadCastMxInfo() =  GlobalScope.launch {
        broadCast(produceMxInfo())
    }


    fun CoroutineScope.produceMxInfo() = produce<MachineInfoDTO> {

        while (true) {

            send(MachineInfoDTO(bean.systemCpuLoad * 100, Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory(), getDbDirSize(dbDir), getFreeDiskSpace(dbDir)))
            delay(3000L)
        }
    }

    fun CoroutineScope.broadCast(channel: ReceiveChannel<MachineInfoDTO>) = launch {

        for (node in channel) {
            mxInfoChannel.send(node)
        }

    }


    suspend fun getDbDirSize(dbDir: File): Long {
        if ((System.currentTimeMillis() - dbSizeMeasurementTime) < DB_SIZE_CACHE_EVICT_MS) {
            return dbSize;
        }

        this.dbSizeMeasurementTime = System.currentTimeMillis();

        try {

            val stream = Files.walk(dbDir.toPath())

            // https://stackoverflow.com/questions/34642254/what-java-8-stream-collect-equivalents-are-available-in-the-standard-kotlin-libr
            // asSequence == lazy so better performance
            dbSize = stream.asSequence().filter { p -> p.toFile().isFile() }.map { it.toFile().length() }.sum()

        } catch (e: IOException) {
            logger.error("Unable to calculate db size", e)
        }

        return dbSize;
    }

    suspend fun getFreeDiskSpace(dbDir: File): Long {
        return dbDir.usableSpace;

    }

}