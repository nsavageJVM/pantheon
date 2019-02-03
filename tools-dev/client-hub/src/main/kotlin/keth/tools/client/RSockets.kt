package keth.tools.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.rsocket.kotlin.DefaultPayload
import io.rsocket.kotlin.Payload
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.RSocketFactory
import io.rsocket.kotlin.transport.netty.server.NettyContextCloseable
import io.rsocket.kotlin.transport.netty.server.WebsocketServerTransport
import io.rsocket.kotlin.util.AbstractRSocket
import keth.tools.client.mx.MachineInfoDTO
import keth.tools.client.mx.MxDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

/**
 * Server Socket Provider
 */
abstract class SocketProvider(private val port: Int) {

    protected val LOG = LoggerFactory.getLogger(this.javaClass.name)
    protected val mapper = jacksonObjectMapper()

    protected val closeable: Single<NettyContextCloseable> = RSocketFactory
            .receive()
            .acceptor { { _, _ -> handler() } } // server handler RSocket
            .transport(WebsocketServerTransport.create("127.0.0.1:9989/tools/mx", port))
            .start()

    init {
        closeable.subscribe({
            LOG.info("subscribed = $it")
        }, {
            LOG.error("it = $it")
        })
    }

    /**
     *  handles in the context taht it pipes the stream out thru the socket
     */
    abstract fun handler(): Single<RSocket>

}

/**
 *  stream mx bean data over ws. with s sec need stomp
 */
//@Controller
//class MxSocket(@Autowired private val mxDataProvider: MxDataProvider) : SocketProvider(9989) {
//
//    val producer: PublishProcessor<MachineInfoDTO> = PublishProcessor.create();
//
//    var stream = Flowable.create<MachineInfoDTO>({ subscriber ->
//
//        producer.subscribe {
//            subscriber.onNext(it);
//        };
//
//    }, BackpressureStrategy.LATEST);
//
//    init {
//        GlobalScope.launch {
//            val brodcastJob = mxDataProvider.broadCastMxInfo()
//            delay(5000)
//            processMachineInfo(mxDataProvider.mxInfoChannel)
//        }
//    }
//
//    override fun handler(): Single<RSocket> {
//        LOG.info("MxSocket handler  handling socket ")
//        return Single.just(object : AbstractRSocket() {
//            override fun requestStream(payload: Payload): Flowable<Payload> {
//                return stream.observeOn(Schedulers.io()).map {
//                    DefaultPayload.text(mapper.writeValueAsString(it))
//                }
//            }
//        })
//    }
//
//    fun CoroutineScope.processMachineInfo(inStream: Channel<MachineInfoDTO>) = launch {
//
//        for (node in inStream) {
//            broadcast(node)
//        }
//    }
//
//    suspend fun broadcast(dto: MachineInfoDTO) {
//        producer.onNext(dto);
//    }
//
//}



