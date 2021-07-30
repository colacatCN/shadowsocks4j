package com.life4ever.shadowsocks4j.handler.remote;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TargetToRemoteHandler extends ChannelInboundHandlerAdapter {

    private final ChannelHandlerContext localChannelHandlerContext;

    public TargetToRemoteHandler(ChannelHandlerContext localChannelHandlerContext) {
        this.localChannelHandlerContext = localChannelHandlerContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        localChannelHandlerContext.writeAndFlush(byteBuf);
    }

}
