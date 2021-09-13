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

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String BOSS_GROUP_THREAD_POOL_NAME = "netty-boss";

    private static final String SERVER_WORKER_GROUP_THREAD_POOL_NAME = "netty-server-worker";

    private static final String CLIENT_WORKER_GROUP_THREAD_POOL_NAME = "netty-client-worker";

    private static final int RUNTIME_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final String serviceName;

    private final SocketAddress publishSocketAddress;

    private final int numOfServerWorkers;

    private final int numOfClientWorkers;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup serverWorkerGroup;

    private EventLoopGroup clientWorkerGroup;

    protected AbstractShadowsocks4jService(
            String serviceName,
            SocketAddress publishSocketAddress,
            int numOfServerWorkers,
            int numOfClientWorkers) {
        this.serviceName = serviceName;
        this.publishSocketAddress = publishSocketAddress;
        this.numOfServerWorkers = numOfServerWorkers;
        this.numOfClientWorkers = numOfClientWorkers;
    }

    protected AbstractShadowsocks4jService(String serviceName, SocketAddress publishSocketAddress) {
        this(serviceName, publishSocketAddress, RUNTIME_AVAILABLE_PROCESSORS << 1, RUNTIME_AVAILABLE_PROCESSORS << 1);
    }

    @Override
    public void start() throws Shadowsocks4jProxyException {
        try {
            ChannelFuture channelFuture = bind(publishSocketAddress).sync();
            log.info("Start {} @ {}.", serviceName, publishSocketAddress);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        } finally {
            shutdownGracefully();
            log.info("Shutdown {} @ {}.", serviceName, publishSocketAddress);
        }
    }

    @Override
    public void shutdownGracefully() {
        bossGroup.shutdownGracefully();
        serverWorkerGroup.shutdownGracefully();
    }

    protected void initialize() {
        ThreadFactory bossGroupThreadFactory = new DefaultThreadFactory(BOSS_GROUP_THREAD_POOL_NAME);
        ThreadFactory serverWorkerGroupThreadFactory = new DefaultThreadFactory(SERVER_WORKER_GROUP_THREAD_POOL_NAME);
        ThreadFactory clientWorkerGroupThreadFactory = new DefaultThreadFactory(CLIENT_WORKER_GROUP_THREAD_POOL_NAME);

        bossGroup = initializeEventLoopGroup(1, bossGroupThreadFactory);
        serverWorkerGroup = initializeEventLoopGroup(numOfServerWorkers, serverWorkerGroupThreadFactory);
        clientWorkerGroup = initializeEventLoopGroup(numOfClientWorkers, clientWorkerGroupThreadFactory);

        serverBootstrap = new ServerBootstrap()
                .group(bossGroup, serverWorkerGroup);
    }

    protected ServerBootstrap serverBootstrap() {
        return serverBootstrap;
    }

    protected EventLoopGroup clientWorkerGroup() {
        return clientWorkerGroup;
    }

    protected abstract EventLoopGroup initializeEventLoopGroup(int numOfThreads, ThreadFactory threadFactory);

    protected abstract ChannelFuture bind(SocketAddress publishSocketAddress);

}
