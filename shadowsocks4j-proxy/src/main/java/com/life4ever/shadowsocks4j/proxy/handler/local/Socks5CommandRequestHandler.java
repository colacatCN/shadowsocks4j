package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
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
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getRemoteServerSocketAddress;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.needRelayToRemoteServer;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private static volatile Socks5CommandRequestHandler instance;

    private final EventLoopGroup clientWorkerGroup;

    private Socks5CommandRequestHandler(EventLoopGroup clientWorkerGroup) {
        this.clientWorkerGroup = clientWorkerGroup;
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

        relayToRemoteServer(ctx, msg);
    }

    private void relayToRemoteServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(clientWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new RemoteToLocalHandler(ctx));
                        pipeline.addLast(ExceptionCaughtHandler.getInstance());
                    }

                });

        if (needRelayToRemoteServer(msg.dstAddr())) {
            SocketAddress remoteServerInetSocketAddress = getRemoteServerSocketAddress();
            bootstrap.connect(remoteServerInetSocketAddress)
                    .addListener((ChannelFutureListener) channelFuture -> {
                        Socks5CommandResponse socks5CommandResponse;
                        if (channelFuture.isSuccess()) {
                            LOG.info("Succeed to connect to remote-server @ {}.", remoteServerInetSocketAddress);
                            ChannelPipeline pipeline = ctx.channel().pipeline();
                            pipeline.addLast(new LocalToRemoteHandler(channelFuture, msg));
                            pipeline.addLast(ExceptionCaughtHandler.getInstance());
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                        } else {
                            LOG.error("Failed to connect to remote-server @ {}.", remoteServerInetSocketAddress);
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                        }
                        ctx.writeAndFlush(socks5CommandResponse);
                    });
        } else {
            SocketAddress targetServerInetSocketAddress = new InetSocketAddress(msg.dstAddr(), msg.dstPort());
            bootstrap.connect(targetServerInetSocketAddress)
                    .addListener((ChannelFutureListener) channelFuture -> {
                        Socks5CommandResponse socks5CommandResponse;
                        if (channelFuture.isSuccess()) {
                            LOG.info("Succeed to connect to target-server @ {}.", targetServerInetSocketAddress);
                            ChannelPipeline pipeline = ctx.channel().pipeline();
                            pipeline.addLast(ExceptionCaughtHandler.getInstance());
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                        } else {
                            LOG.error("Failed to connect to target-server @ {}.", targetServerInetSocketAddress);
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                        }
                        ctx.writeAndFlush(socks5CommandResponse);
                    });
        }
    }

    public static Socks5CommandRequestHandler getInstance(EventLoopGroup clientWorkerGroup) {
        if (instance == null) {
            synchronized (Socks5CommandRequestHandler.class) {
                if (instance == null) {
                    instance = new Socks5CommandRequestHandler(clientWorkerGroup);
                }
            }
        }
        return instance;
    }

}
