package keth.tools.client.gui

import io.rsocket.kotlin.DefaultPayload
import io.rsocket.kotlin.RSocketFactory
import io.rsocket.kotlin.transport.netty.client.WebsocketClientTransport

import keth.tools.client.mx.MachineInfoDTO
import keth.tools.client.mx.MxDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import java.util.concurrent.CountDownLatch

@RunWith(SpringRunner::class)
@SpringBootTest
class ClientGuiApplicationTests {


	private val keth_hub_status_uri = URI.create("ws://localhost:9989/")

	@Autowired
	lateinit var mxDataProvider: MxDataProvider;

//	@Autowired
//	lateinit var mxSocket: MxSocket;

	val latch =  CountDownLatch(1);

	@Test
	fun mxDataTest() {

		GlobalScope.launch {
			val brodcastJob = mxDataProvider.broadCastMxInfo()
			delay(5000)
			processMachineInfo(mxDataProvider.mxInfoChannel)
		}

		latch.await();
	}


	@Test
	fun mxDataControllerTest() {

		val	client  = RSocketFactory.connect().transport(WebsocketClientTransport.create(keth_hub_status_uri)).start()

		client.subscribe { rs ->  rs.requestStream(DefaultPayload.text("peace"))
				.subscribe { p -> println(p.dataUtf8) } }

		latch.await();
	}



	fun CoroutineScope.processMachineInfo(inStream: Channel<MachineInfoDTO>)  = launch {

		for (node in inStream) {
			println(node)
		}

	}


}


