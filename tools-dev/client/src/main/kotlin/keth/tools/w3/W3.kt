package keth.tools.w3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient

import keth.tools.account.WalletProvider


class Error(code: Int, message: String )

@JsonIgnoreProperties(ignoreUnknown = true)
class JsonResponse<T>(val id:Long, val jsonrpc:T, val result:String, val error: Error? )

// https://vertx.io/docs/vertx-web-client/kotlin/
abstract class W3Base: io.vertx.core.AbstractVerticle()  {

    lateinit var client: WebClient

    val host = "localhost";
    val port = 8545
    val objectMapper = jacksonObjectMapper()

    override fun start() {
        client = WebClient.create(vertx)
    }

    abstract  fun getAccount()


    // == API

    fun  getBalance(id_:Int, acct:String): ObjectNode {
        val node: ObjectNode =  makeBaseObject(id_);
        val p_values =  node.putArray("params");
        p_values.add(acct);
        p_values.add("latest");
        return node.put("method", "eth_getBalance");
    }


    private fun makeBaseObject(id_: Int): ObjectNode {
        var node = JsonNodeFactory.instance.objectNode();
        node.put("jsonrpc", "2.0").put("id", id_)
        return node;
    }

}


class W3Client: W3Base() {


    override fun getAccount() {
        var apiMssg = getBalance(42, WalletProvider.acctCB);
        var json = JsonObject(apiMssg.toString())

        client.post(port, host, "/")
                .sendJsonObject(json)
                { ar ->  if (ar.succeeded()) {
                    val res  = objectMapper.readValue(ar.result().bodyAsString(), JsonResponse::class.java)
                    print( res.result)
                    } else {
                        println(ar.cause().localizedMessage)}
                }
    }

}
 fun main() {
    val vertx = Vertx.vertx()
    val w3Client = W3Client()

    vertx.deployVerticle(w3Client) {
        if(it.succeeded()  ) {
            w3Client.getAccount()
        }
        else { println(it.cause().localizedMessage)}   }

 }