package com.life4ever.shadowsocks4j.proxy.service.impl;

import com.life4ever.shadowsocks4j.proxy.handler.remote.RemoteServerChannelInitializer;
import com.life4ever.shadowsocks4j.proxy.service.AbstractShadowsocks4jService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.REMOTE_SERVER_SERVICE_NAME;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getRemoteServerSocketAddress;

public class Shadowsocks4jRemoteServiceImpl extends AbstractShadowsocks4jService {

    public Shadowsocks4jRemoteServiceImpl() {
        super(REMOTE_SERVER_SERVICE_NAME, getRemoteServerSocketAddress());
        this.initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        serverBootstrap()
                .option(ChannelOption.SO_BACKLOG, 32768)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
    }

    @Override
    protected EventLoopGroup initializeEventLoopGroup(int numOfThreads, ThreadFactory threadFactory) {
        return new NioEventLoopGroup(numOfThreads, threadFactory);
    }

    @Override
    protected ChannelFuture bind(SocketAddress publishSocketAddress) {
        return serverBootstrap()
                .channel(NioServerSocketChannel.class)
                .childHandler(new RemoteServerChannelInitializer())
                .bind(publishSocketAddress);
    }

}
