package com.life4ever.shadowsocks4j.proxy.service;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractShadowsocks4jService implements IShadowsocks4jService {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final SocketAddress publishSocketAddress;

    private final int numOfWorkers;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    protected AbstractShadowsocks4jService(SocketAddress publishSocketAddress, int numOfWorkers) {
        this.publishSocketAddress = publishSocketAddress;
        this.numOfWorkers = numOfWorkers;
    }

    protected AbstractShadowsocks4jService(SocketAddress publishSocketAddress) {
        this(publishSocketAddress, AVAILABLE_PROCESSORS << 1);
    }

    @Override
    public void start() throws Shadowsocks4jProxyException {
        try {
            ChannelFuture channelFuture = bind(publishSocketAddress).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    @Override
    public void shutdownGracefully() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    protected void initialize() {
        ThreadFactory bossGroupThreadFactory = new DefaultThreadFactory("netty-boss");
        ThreadFactory workerGroupThreadFactory = new DefaultThreadFactory("netty-worker");

        bossGroup = initializeEventLoopGroup(1, bossGroupThreadFactory);
        workerGroup = initializeEventLoopGroup(numOfWorkers, workerGroupThreadFactory);

        serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup);
    }

    protected ServerBootstrap serverBootstrap() {
        return serverBootstrap;
    }

    protected SocketAddress publishSocketAddress() {
        return publishSocketAddress;
    }

    protected abstract EventLoopGroup initializeEventLoopGroup(int numOfThreads, ThreadFactory threadFactory);

    protected abstract ChannelFuture bind(SocketAddress publishSocketAddress);

}
