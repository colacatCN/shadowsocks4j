package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.life4ever.shadowsocks4j.proxy.handler.bootstrap.LocalClientBootstrap.localToRemoteClientBootstrap;
import static com.life4ever.shadowsocks4j.proxy.handler.bootstrap.LocalClientBootstrap.localToTargetClientBootstrap;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.needRelayToRemoteServer;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.remoteServerSocketAddress;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private Socks5CommandRequestHandler() {
    }

    public static Socks5CommandRequestHandler getInstance() {
        return Socks5CommandRequestHandlerHolder.INSTANCE;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        if (!msg.decoderResult().isSuccess()) {
            throw new Shadowsocks4jProxyException("SOCKS protocol version is not 5.");
        }

        if (!Socks5CommandType.CONNECT.equals(msg.type())) {
            throw new Shadowsocks4jProxyException("SOCKS command type is not CONNECT.");
        }

        SocketAddress clientSocketAddress = ctx.channel().remoteAddress();
        LOG.info("Start channel @ {}.", clientSocketAddress);

        connectTo(ctx, msg);
    }

    private void connectTo(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        if (needRelayToRemoteServer(msg.dstAddr())) {
            connectToRemoteServer(ctx, msg);
        } else {
            connectToTargetServer(ctx, msg);
        }
    }

    private void connectToRemoteServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        SocketAddress remoteServerSocketAddress = remoteServerSocketAddress();
        localToRemoteClientBootstrap()
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

    private void connectToTargetServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        SocketAddress targetServerSocketAddress = new InetSocketAddress(msg.dstAddr(), msg.dstPort());
        localToTargetClientBootstrap()
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

}
