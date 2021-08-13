package com.life4ever.shadowsocks4j.proxy.handler.remote;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteToTargetHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteToTargetHandler.class);

    private final EventLoopGroup clientWorkerGroup;

    private final InetSocketAddress targetServerInetSocketAddress;

    private final AtomicReference<Channel> channelAtomicReference;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    public RemoteToTargetHandler(EventLoopGroup clientWorkerGroup,
                                 InetSocketAddress targetServerInetSocketAddress,
                                 ChannelHandlerContext localChannelHandlerContext) {
        this.clientWorkerGroup = clientWorkerGroup;
        this.targetServerInetSocketAddress = targetServerInetSocketAddress;
        this.channelAtomicReference = new AtomicReference<>();
        relayToTargetServer(localChannelHandlerContext);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        lock.lock();
        try {
            Channel channel;
            while ((channel = channelAtomicReference.get()) == null) {
                if (!condition.await(60000L, TimeUnit.MILLISECONDS)) {
                    throw new Shadowsocks4jProxyException("Failed to get channel within 60 seconds.");
                }
            }
            channel.writeAndFlush(byteBuf);
        } finally {
            lock.unlock();
        }
    }

    private void relayToTargetServer(ChannelHandlerContext localChannelHandlerContext) {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(clientWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new TargetToRemoteHandler(localChannelHandlerContext));
                        pipeline.addLast(ExceptionCaughtHandler.getInstance());
                    }

                });

        bootstrap.connect(targetServerInetSocketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        lock.lock();
                        try {
                            LOG.info("Succeed to connect to target server @ {}.", targetServerInetSocketAddress);
                            channelAtomicReference.set(future.channel());
                            condition.signal();
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        LOG.error("Failed to connect to target server @ {}.", targetServerInetSocketAddress);
                    }
                });
    }

}
