package net.consensys.pantheon.ethereum.p2p.netty;

import net.consensys.pantheon.ethereum.p2p.api.MessageData;
import net.consensys.pantheon.ethereum.p2p.api.PeerConnection;
import net.consensys.pantheon.ethereum.p2p.netty.exceptions.IncompatiblePeerException;
import net.consensys.pantheon.ethereum.p2p.rlpx.framing.Framer;
import net.consensys.pantheon.ethereum.p2p.wire.PeerInfo;
import net.consensys.pantheon.ethereum.p2p.wire.SubProtocol;
import net.consensys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;
import net.consensys.pantheon.ethereum.p2p.wire.messages.HelloMessage;
import net.consensys.pantheon.ethereum.p2p.wire.messages.WireMessageCodes;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class DeFramer extends ByteToMessageDecoder {

  private static final Logger LOG = LogManager.getLogger(DeFramer.class);

  private final CompletableFuture<PeerConnection> connectFuture;

  private final Callbacks callbacks;

  private final Framer framer;
  private final PeerInfo ourInfo;
  private final List<SubProtocol> subProtocols;
  private boolean hellosExchanged;

  DeFramer(
      final Framer framer,
      final List<SubProtocol> subProtocols,
      final PeerInfo ourInfo,
      final Callbacks callbacks,
      final CompletableFuture<PeerConnection> connectFuture) {
    this.framer = framer;
    this.subProtocols = subProtocols;
    this.ourInfo = ourInfo;
    this.connectFuture = connectFuture;
    this.callbacks = callbacks;
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
    MessageData message;
    while ((message = framer.deframe(in)) != null) {

      if (!hellosExchanged && message.getCode() == WireMessageCodes.HELLO) {
        hellosExchanged = true;
        // Decode first hello and use the payload to modify pipeline
        final PeerInfo peerInfo = parsePeerInfo(message);
        message.release();
        LOG.debug("Received HELLO message: {}", peerInfo);
        if (peerInfo.getVersion() >= 5) {
          LOG.debug("Enable compression for p2pVersion: {}", peerInfo.getVersion());
          framer.enableCompression();
        }

        final CapabilityMultiplexer capabilityMultiplexer =
            new CapabilityMultiplexer(
                subProtocols, ourInfo.getCapabilities(), peerInfo.getCapabilities());
        final PeerConnection connection =
            new NettyPeerConnection(ctx, peerInfo, capabilityMultiplexer, callbacks);
        if (capabilityMultiplexer.getAgreedCapabilities().size() == 0) {
          LOG.info(
              "Disconnecting from {} because no capabilities are shared.", peerInfo.getClientId());
          connectFuture.completeExceptionally(
              new IncompatiblePeerException("No shared capabilities"));
          connection.disconnect(DisconnectReason.USELESS_PEER);
          return;
        }

        // Setup next stage
        final AtomicBoolean waitingForPong = new AtomicBoolean(false);
        ctx.channel()
            .pipeline()
            .addLast(
                new IdleStateHandler(15, 0, 0),
                new WireKeepAlive(connection, waitingForPong),
                new ApiHandler(capabilityMultiplexer, connection, callbacks, waitingForPong),
                new MessageFramer(capabilityMultiplexer, framer));
        connectFuture.complete(connection);
      } else {
        out.add(message);
      }
    }
  }

  private PeerInfo parsePeerInfo(final MessageData message) {
    final HelloMessage helloMessage = HelloMessage.readFrom(message);
    final PeerInfo peerInfo = helloMessage.getPeerInfo();
    helloMessage.release();
    return peerInfo;
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable throwable)
      throws Exception {
    LOG.error("Exception while processing incoming message", throwable);
    if (connectFuture.isDone()) {
      connectFuture.get().terminateConnection(DisconnectReason.TCP_SUBSYSTEM_ERROR, false);
    } else {
      connectFuture.completeExceptionally(throwable);
      ctx.close();
    }
  }
}