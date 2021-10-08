package com.life4ever.shadowsocks4j.proxy.handler.remote;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.EXCEPTION_CAUGHT_HANDLER_NAME;
import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.TARGET_TO_REMOTE_HANDLER_NAME;
import static com.life4ever.shadowsocks4j.proxy.handler.bootstrap.RemoteClientBootstrap.remoteClientBootstrap;

public class RemoteToTargetHandler extends ChannelInboundHandlerAdapter {

    private static final long CONNECT_TIMEOUT = 60000L;

    private static final Logger LOG = LoggerFactory.getLogger(RemoteToTargetHandler.class);

    private final SocketAddress targetServerSocketAddress;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private Channel channel;

    public RemoteToTargetHandler(SocketAddress targetServerSocketAddress, ChannelHandlerContext localChannelHandlerContext) {
        this.targetServerSocketAddress = targetServerSocketAddress;
        connectToTargetServer(targetServerSocketAddress, localChannelHandlerContext);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        lock.lock();
        try {
            while (channel == null) {
                if (!condition.await(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    throw new Shadowsocks4jProxyException("Failed to connect to target server @ "
                            + targetServerSocketAddress
                            + " within 60 seconds.");
                }
            }
            channel.writeAndFlush(byteBuf);
        } finally {
            lock.unlock();
        }
    }

    private void connectToTargetServer(SocketAddress targetServerSocketAddress, ChannelHandlerContext localChannelHandlerContext) {
        remoteClientBootstrap()
                .connect(targetServerSocketAddress)
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        lock.lock();
                        try {
                            LOG.info("Succeed to connect to target server @ {}.", targetServerSocketAddress);
                            ChannelPipeline pipeline = channelFuture.channel().pipeline();
                            pipeline.addBefore(EXCEPTION_CAUGHT_HANDLER_NAME, TARGET_TO_REMOTE_HANDLER_NAME, new TargetToRemoteHandler(localChannelHandlerContext));
                            channel = channelFuture.channel();
                            condition.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                });
    }

}
