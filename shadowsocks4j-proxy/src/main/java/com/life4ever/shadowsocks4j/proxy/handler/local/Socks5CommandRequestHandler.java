package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
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

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.RUNTIME_AVAILABLE_PROCESSORS;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getRemoteServerSocketAddress;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.needRelayToRemoteServer;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(RUNTIME_AVAILABLE_PROCESSORS << 1);

    private Socks5CommandRequestHandler() {
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.channel().close();
    }

    private void relayToRemoteServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new RemoteToLocalHandler(ctx));
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
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                        } else {
                            LOG.error("Failed to connect to target-server @ {}.", targetServerInetSocketAddress);
                            socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                        }
                        ctx.writeAndFlush(socks5CommandResponse);
                    });
        }
    }

    public static Socks5CommandRequestHandler getInstance() {
        return Socks5CommandRequestHandlerHolder.INSTANCE;
    }

    private static class Socks5CommandRequestHandlerHolder {

        private static final Socks5CommandRequestHandler INSTANCE = new Socks5CommandRequestHandler();

    }

}
