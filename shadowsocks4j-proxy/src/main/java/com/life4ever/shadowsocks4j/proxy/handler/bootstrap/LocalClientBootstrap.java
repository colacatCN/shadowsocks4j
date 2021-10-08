package com.life4ever.shadowsocks4j.proxy.handler.bootstrap;

import com.life4ever.shadowsocks4j.proxy.handler.common.CipherDecryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherEncryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.EXCEPTION_CAUGHT_HANDLER_NAME;

public class LocalClientBootstrap {

    private static EventLoopGroup clientWorkerGroup;

    private LocalClientBootstrap() {
    }

    public static Bootstrap getInstance() {
        return LocalClientBootstrapHolder.INSTANCE;
    }

    public static void init(EventLoopGroup eventLoopGroup) {
        clientWorkerGroup = eventLoopGroup;
    }

    public static Bootstrap localToRemoteClientBootstrap() {
        return getInstance()
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addFirst(CipherEncryptHandler.getInstance());
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(CipherDecryptHandler.getInstance());
                        pipeline.addLast(EXCEPTION_CAUGHT_HANDLER_NAME, ExceptionCaughtHandler.getInstance());
                    }

                });
    }

    public static Bootstrap localToTargetClientBootstrap() {
        return getInstance()
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(EXCEPTION_CAUGHT_HANDLER_NAME, ExceptionCaughtHandler.getInstance());
                    }

                });
    }

    private static class LocalClientBootstrapHolder {

        private static final Bootstrap INSTANCE = new Bootstrap()
                .group(clientWorkerGroup)
                .channel(NioSocketChannel.class);

    }

}
