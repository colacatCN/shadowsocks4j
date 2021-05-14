package com.life4ever.shadowsocks4j.service.server.handler;

import com.life4ever.shadowsocks4j.util.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RemoteWriteBackHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {
        Channel localChannel = ChannelUtil.getLocalChannelFromRemoteChannelMap(ctx.channel());
        localChannel.writeAndFlush(msg);
    }

}
