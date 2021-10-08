package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.HeartbeatTimeoutHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_ALL_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_READ_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_WRITE_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.EXCEPTION_CAUGHT_HANDLER_NAME;
import static com.life4ever.shadowsocks4j.proxy.handler.bootstrap.LocalClientBootstrap.init;

public class LocalServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    public LocalServerChannelInitializer(EventLoopGroup clientWorkerGroup) {
        init(clientWorkerGroup);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        // encoder
        pipeline.addFirst(Socks5ServerEncoder.DEFAULT);

        // heartbeat
        pipeline.addLast(new IdleStateHandler(SERVER_READ_IDLE_TIME, SERVER_WRITE_IDLE_TIME, SERVER_ALL_IDLE_TIME, TimeUnit.MILLISECONDS));
        pipeline.addLast(HeartbeatTimeoutHandler.getInstance());

        // init
        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(Socks5InitialRequestHandler.getInstance());

        // command
        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(Socks5CommandRequestHandler.getInstance());

        // exception
        pipeline.addLast(EXCEPTION_CAUGHT_HANDLER_NAME, ExceptionCaughtHandler.getInstance());
    }

}
