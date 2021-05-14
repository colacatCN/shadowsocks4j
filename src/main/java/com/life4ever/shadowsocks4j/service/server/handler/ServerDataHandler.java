package com.life4ever.shadowsocks4j.service.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life4ever.shadowsocks4j.pojo.Shadowsocks4jDTO;
import com.life4ever.shadowsocks4j.util.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerDataHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        Shadowsocks4jDTO shadowsocks4jDTO = MAPPER.readValue(msg, Shadowsocks4jDTO.class);

        Channel localChannel = ctx.channel();
        Channel remoteChannel;
        if ((remoteChannel = ChannelUtil.getRemoteChannel(localChannel)) == null) {
            remoteChannel = ChannelUtil.createRemoteChannel(shadowsocks4jDTO.getHostname(), shadowsocks4jDTO.getPort());
            ChannelUtil.putRemoteChannel(localChannel, remoteChannel);
        }

        byte[] payload = shadowsocks4jDTO.getPayload();
        remoteChannel.writeAndFlush(payload);
    }

}
