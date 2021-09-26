package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherDecryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherEncryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.needRelayToRemoteServer;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.remoteServerSocketAddress;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private static EventLoopGroup clientWorkerGroup;

    private Socks5CommandRequestHandler() {
    }

    public static Socks5CommandRequestHandler getInstance() {
        return Socks5CommandRequestHandlerHolder.INSTANCE;
    }

    public static void init(EventLoopGroup eventLoopGroup) {
        clientWorkerGroup = eventLoopGroup;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        if (!msg.decoderResult().isSuccess()) {
            throw new Shadowsocks4jProxyException("SOCKS protocol version is not 5.");
        }

        if (!Socks5CommandType.CONNECT.equals(msg.type())) {
            throw new Shadowsocks4jProxyException("SOCKS command type is not CONNECT.");
        }

        SocketAddress clientInetSocketAddress = ctx.channel().remoteAddress();
        LOG.info("Start channel @ {}.", clientInetSocketAddress);

        relayTo(ctx, msg);
    }

    private void relayTo(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        if (needRelayToRemoteServer(msg.dstAddr())) {
            relayToRemoteServer(ctx, msg);
        } else {
            relayToLocalServer(ctx, msg);
        }
    }

    private void relayToRemoteServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        SocketAddress remoteServerSocketAddress = remoteServerSocketAddress();
        remoteBootstrapInstance()
                .connect(remoteServerSocketAddress)
                .addListener((ChannelFutureListener) channelFuture -> {
                    Socks5CommandResponse socks5CommandResponse;
                    if (channelFuture.isSuccess()) {
                        LOG.info("Succeed to connect to remote-server @ {}.", remoteServerSocketAddress);
                        // 增加处理 remote 响应数据的 handler
                        ChannelPipeline localToRemotePipeline = channelFuture.channel().pipeline();
                        localToRemotePipeline.addLast(new ResponseMsgHandler(ctx));
                        localToRemotePipeline.addLast(ExceptionCaughtHandler.getInstance());
                        // 增加处理 client 请求数据的 handler
                        ChannelPipeline clientToLocalPipeline = ctx.channel().pipeline();
                        clientToLocalPipeline.addLast(new LocalToRemoteHandler(channelFuture, msg));
                        clientToLocalPipeline.addLast(ExceptionCaughtHandler.getInstance());
                        // 返回 sock5 建立成功的响应
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                    } else {
                        LOG.error("Failed to connect to remote-server @ {}.", remoteServerSocketAddress);
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                    }
                    ctx.writeAndFlush(socks5CommandResponse);
                });
    }

    private void relayToLocalServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        SocketAddress targetServerSocketAddress = new InetSocketAddress(msg.dstAddr(), msg.dstPort());
        localBootstrapInstance()
                .connect(targetServerSocketAddress)
                .addListener((ChannelFutureListener) channelFuture -> {
                    Socks5CommandResponse socks5CommandResponse;
                    if (channelFuture.isSuccess()) {
                        LOG.info("Succeed to connect to target-server @ {}.", targetServerSocketAddress);
                        // 增加处理 target 响应数据的 handler
                        ChannelPipeline localToTargetPipeline = channelFuture.channel().pipeline();
                        localToTargetPipeline.addLast(new ResponseMsgHandler(ctx));
                        localToTargetPipeline.addLast(ExceptionCaughtHandler.getInstance());
                        // 增加处理 client 请求数据的 handler
                        ChannelPipeline clientToLocalPipeline = ctx.channel().pipeline();
                        clientToLocalPipeline.addLast(new LocalToTargetHandler(channelFuture));
                        clientToLocalPipeline.addLast(ExceptionCaughtHandler.getInstance());
                        // 返回 sock5 建立成功的响应
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                    } else {
                        LOG.error("Failed to connect to target-server @ {}.", targetServerSocketAddress);
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                    }
                    ctx.writeAndFlush(socks5CommandResponse);
                });
    }

    private static class Socks5CommandRequestHandlerHolder {

        private static final Socks5CommandRequestHandler INSTANCE = new Socks5CommandRequestHandler();

    }

    private static class RemoteBootstrapHolder {

        private static final Bootstrap INSTANCE = new Bootstrap()
                .group(clientWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addFirst(CipherEncryptHandler.getInstance());
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(CipherDecryptHandler.getInstance());
                    }

                });

    }

    private static class LocalBootstrapHolder {

        private static final Bootstrap INSTANCE = new Bootstrap()
                .group(clientWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        // do nothing
                    }

                });

    }

    private static Bootstrap remoteBootstrapInstance() {
        return RemoteBootstrapHolder.INSTANCE;
    }

    private static Bootstrap localBootstrapInstance() {
        return LocalBootstrapHolder.INSTANCE;
    }

}
