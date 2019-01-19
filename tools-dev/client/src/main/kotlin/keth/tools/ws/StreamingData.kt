package keth.tools.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.Json
import keth.tools.Constants
import keth.tools.mx.MachineInfoDTO
import keth.tools.mx.MxSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WebSocketRequestHandler(val vertx:Vertx) {

    val objectMapper = jacksonObjectMapper()

    val dto = MachineInfoDTO(0.0, 0L ,0L, 0L, 0L)

    fun handle(connectionId: String, buffer: Buffer, ws: ServerWebSocket) {

        println("WebSocketRequestHandler  ${connectionId}  ::::   ${String(buffer.byteBuf.array(), Charsets.UTF_8 )}")

        replyToClient(connectionId,    Json.encodeToBuffer( dto),  ws )
    }

    private fun replyToClient(id: String, buffer: Buffer, ws: ServerWebSocket) {
        GlobalScope.launch {
            MxSource.broadCastMxInfo()
            delay(5000)
            for (node in MxSource.mxInfoChannel) {
                ws.writeFinalTextFrame(  objectMapper.writeValueAsString(node))
            }
        }
    }
}

object WsArtifacts {

    fun getWebSocketHandler(vertx: Vertx): Handler<ServerWebSocket> {
        var  websocketRequestHandler = WebSocketRequestHandler(vertx)
        return object: Handler<ServerWebSocket>   {
            override fun handle(websocket: ServerWebSocket?) {
                val socketAddress = websocket!!.remoteAddress();
                val connectionId = websocket.textHandlerID();

                websocket.handler { buffer: Buffer ->
                    websocketRequestHandler.handle(connectionId, buffer, websocket);
                }

                websocket.closeHandler {
                    v ->
                    println("Websocket Disconnected  ${socketAddress}")
                    vertx.eventBus().publish(Constants.REMOVE_SUBSCRIPTIONS_ADDRESS, connectionId);
                }

                websocket.exceptionHandler {
                    println("Unrecoverable error on Websocket ${it.localizedMessage}")
                    websocket.close()
                }
            }
        }
    }

}
