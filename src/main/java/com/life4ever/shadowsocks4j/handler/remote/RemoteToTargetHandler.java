package com.life4ever.shadowsocks4j.handler.remote;

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

public class RemoteToTargetHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteToTargetHandler.class);

    private final InetSocketAddress targetServerInetSocketAddress;

    private Channel channel;

    public RemoteToTargetHandler(InetSocketAddress targetServerInetSocketAddress, ChannelHandlerContext localChannelHandlerContext) {
        this.targetServerInetSocketAddress = targetServerInetSocketAddress;
        relayToTargetServer(localChannelHandlerContext);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (channel != null) {
            channel.writeAndFlush(byteBuf);
        }
    }

    private void relayToTargetServer(ChannelHandlerContext localChannelHandlerContext) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

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
                        LOG.info("成功连接 target-server");
                        channel = future.channel();
                    } else {
                        LOG.info("无法连接 target-server");
                    }
                });

    }

}
