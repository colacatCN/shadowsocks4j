package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;

public class LocalServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst(Socks5ServerEncoder.DEFAULT);
        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(new Socks5InitialRequestHandler());
        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(new Socks5CommandRequestHandler());
    }

}
