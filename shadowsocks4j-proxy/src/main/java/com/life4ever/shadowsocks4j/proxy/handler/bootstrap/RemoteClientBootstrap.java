package com.life4ever.shadowsocks4j.proxy.handler.bootstrap;

import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.EXCEPTION_CAUGHT_HANDLER_NAME;

public class RemoteClientBootstrap {

    private static EventLoopGroup clientWorkerGroup;

    private RemoteClientBootstrap() {
    }

    public static Bootstrap getInstance() {
        return RemoteClientBootstrapHolder.INSTANCE;
    }

    public static void init(EventLoopGroup eventLoopGroup) {
        clientWorkerGroup = eventLoopGroup;
    }

    public static Bootstrap remoteClientBootstrap() {
        return getInstance()
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(EXCEPTION_CAUGHT_HANDLER_NAME, ExceptionCaughtHandler.getInstance());
                    }

                });
    }

    private static class RemoteClientBootstrapHolder {

        private static final Bootstrap INSTANCE = new Bootstrap()
                .group(clientWorkerGroup)
                .channel(NioSocketChannel.class);

    }

}
