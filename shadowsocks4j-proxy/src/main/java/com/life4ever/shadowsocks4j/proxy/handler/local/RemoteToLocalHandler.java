package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteToLocalHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(LocalToRemoteHandler.class);

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
