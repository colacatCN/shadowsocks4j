package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LocalToTargetHandler extends ChannelInboundHandlerAdapter {

    private final ChannelFuture localServerChannelFuture;

    public LocalToTargetHandler(ChannelFuture localServerChannelFuture) {
        this.localServerChannelFuture = localServerChannelFuture;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        localServerChannelFuture.channel().writeAndFlush(msg);
    }

}
