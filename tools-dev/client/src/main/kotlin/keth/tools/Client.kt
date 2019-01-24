package keth.tools

import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.http.HttpServerOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import keth.tools.db.bootStrapDbArtifacts
import keth.tools.utils.bootStrapChainArtifacts
import keth.tools.ws.WsArtifacts

suspend fun main() {

    val vertx = Vertx.vertx()
    try {
        vertx.deployVerticleAwait("keth.tools.Client")
        println("Application started")
    } catch (exception: Throwable) {
        println("Could not start application")
        exception.printStackTrace()
    }
}



class Client : CoroutineVerticle() {

    override suspend fun start() {
        vertx.createHttpServer(
                HttpServerOptions().setHost("localhost").setPort(9999).setWebsocketSubProtocols("undefined") )
                .websocketHandler(WsArtifacts.getWebSocketHandler(vertx))
                .listen()
    }

}