package com.life4ever.shadowsocks4j.proxy.handler.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteToTargetHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteToTargetHandler.class);

    private final NioEventLoopGroup workerGroup;

    private final InetSocketAddress targetServerInetSocketAddress;

    private final AtomicReference<Channel> channelAtomicReference;

    public RemoteToTargetHandler(NioEventLoopGroup workerGroup, InetSocketAddress targetServerInetSocketAddress, ChannelHandlerContext localChannelHandlerContext) {
        this.workerGroup = workerGroup;
        this.targetServerInetSocketAddress = targetServerInetSocketAddress;
        this.channelAtomicReference = new AtomicReference<>();
        relayToTargetServer(localChannelHandlerContext);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        for (; ; ) {
            Channel channel = channelAtomicReference.get();
            if (channel != null) {
                channel.writeAndFlush(byteBuf);
                break;
            }
        }
    }

    private void relayToTargetServer(ChannelHandlerContext localChannelHandlerContext) {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new TargetToRemoteHandler(localChannelHandlerContext));
                    }

                });

        bootstrap.connect(targetServerInetSocketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        LOG.info("Succeed to connect to target server @ {}.", targetServerInetSocketAddress);
                        channelAtomicReference.set(future.channel());
                    } else {
                        LOG.error("Failed to connect to target server @ {}.", targetServerInetSocketAddress);
                    }
                });
    }

}
