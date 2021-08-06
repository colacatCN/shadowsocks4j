package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class LocalServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        // encoder
        pipeline.addFirst(Socks5ServerEncoder.DEFAULT);

        // heartbeat
        pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(LocalHeartbeatHandler.getInstance());

        // init
        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(Socks5InitialRequestHandler.getInstance());

        // command
        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(Socks5CommandRequestHandler.getInstance());
    }

}
