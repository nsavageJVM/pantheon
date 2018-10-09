package net.consensys.pantheon.ethereum.jsonrpc.websocket;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import net.consensys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import net.consensys.pantheon.ethereum.jsonrpc.websocket.methods.WebSocketMethodsFactory;
import net.consensys.pantheon.ethereum.jsonrpc.websocket.subscription.SubscriptionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.WebSocketBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class WebSocketServiceTest {

  private static final int VERTX_AWAIT_TIMEOUT_MILLIS = 10000;

  private Vertx vertx;
  private WebSocketConfiguration websocketConfiguration;
  private WebSocketRequestHandler webSocketRequestHandlerSpy;
  private WebSocketService websocketService;

  @Before
  public void before() {
    vertx = Vertx.vertx();

    websocketConfiguration = WebSocketConfiguration.createDefault();
    final Map<String, JsonRpcMethod> websocketMethods =
        new WebSocketMethodsFactory(new SubscriptionManager(), new HashMap<>()).methods();
    webSocketRequestHandlerSpy = spy(new WebSocketRequestHandler(vertx, websocketMethods));

    websocketService =
        new WebSocketService(vertx, websocketConfiguration, webSocketRequestHandlerSpy);
    websocketService.start().join();
  }

  @After
  public void after() {
    reset(webSocketRequestHandlerSpy);
    websocketService.stop();
  }

  @Test
  public void websocketServiceExecutesHandlerOnMessage(final TestContext context) {
    final Async async = context.async();

    final String request = "{\"id\": 1, \"method\": \"eth_subscribe\", \"params\": [\"syncing\"]}";
    final String expectedResponse = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"0x1\"}";

    vertx
        .createHttpClient()
        .websocket(
            websocketConfiguration.getPort(),
            websocketConfiguration.getHost(),
            "/",
            webSocket -> {
              webSocket.write(Buffer.buffer(request));

              webSocket.handler(
                  buffer -> {
                    context.assertEquals(expectedResponse, buffer.toString());
                    async.complete();
                  });
            });

    async.awaitSuccess(VERTX_AWAIT_TIMEOUT_MILLIS);
  }

  @Test
  public void websocketServiceRemoveSubscriptionOnConnectionClose(final TestContext context) {
    final Async async = context.async();

    vertx
        .eventBus()
        .consumer(SubscriptionManager.EVENTBUS_REMOVE_SUBSCRIPTIONS_ADDRESS)
        .handler(
            m -> {
              context.assertNotNull(m.body());
              async.complete();
            })
        .completionHandler(
            v ->
                vertx
                    .createHttpClient()
                    .websocket(
                        websocketConfiguration.getPort(),
                        websocketConfiguration.getHost(),
                        "/",
                        WebSocketBase::close));

    async.awaitSuccess(VERTX_AWAIT_TIMEOUT_MILLIS);
  }

  @Test
  public void websocketServiceCloseConnectionOnUnrecoverableError(final TestContext context) {
    final Async async = context.async();

    final byte[] bigMessage = new byte[HttpServerOptions.DEFAULT_MAX_WEBSOCKET_MESSAGE_SIZE + 1];
    Arrays.fill(bigMessage, (byte) 1);

    vertx
        .createHttpClient()
        .websocket(
            websocketConfiguration.getPort(),
            websocketConfiguration.getHost(),
            "/",
            webSocket -> {
              webSocket.write(Buffer.buffer(bigMessage));
              webSocket.closeHandler(v -> async.complete());
            });

    async.awaitSuccess(VERTX_AWAIT_TIMEOUT_MILLIS);
  }
}