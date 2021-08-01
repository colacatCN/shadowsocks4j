package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RemoteToLocalHandler extends ChannelInboundHandlerAdapter {

    private final ChannelHandlerContext clientChannelHandlerContext;

    public RemoteToLocalHandler(ChannelHandlerContext clientChannelHandlerContext) {
        this.clientChannelHandlerContext = clientChannelHandlerContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        clientChannelHandlerContext.writeAndFlush(byteBuf);
    }

}
