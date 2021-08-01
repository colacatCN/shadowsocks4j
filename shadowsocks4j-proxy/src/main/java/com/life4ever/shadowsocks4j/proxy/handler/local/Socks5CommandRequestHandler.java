package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getRemoteServerInetSocketAddress;

public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        if (msg.decoderResult().isSuccess() && Socks5CommandType.CONNECT.equals(msg.type())) {
            relayToRemoteServer(ctx, msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void relayToRemoteServer(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new RemoteToLocalHandler(ctx));
                    }
                });

        InetSocketAddress remoteServerInetSocketAddress = getRemoteServerInetSocketAddress();
        bootstrap.connect(remoteServerInetSocketAddress)
                .addListener((ChannelFutureListener) channelFuture -> {
                    Socks5CommandResponse socks5CommandResponse;
                    if (channelFuture.isSuccess()) {
                        LOG.info("Succeed to connect to remote-server @ {}.", remoteServerInetSocketAddress);
                        ChannelPipeline pipeline = ctx.channel().pipeline();
                        pipeline.addLast(new LocalToRemoteHandler(channelFuture, msg));
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    } else {
                        LOG.error("Failed to connect to remote-server @ {}.", remoteServerInetSocketAddress);
                        socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    }
                    ctx.writeAndFlush(socks5CommandResponse);
                });
    }

}
