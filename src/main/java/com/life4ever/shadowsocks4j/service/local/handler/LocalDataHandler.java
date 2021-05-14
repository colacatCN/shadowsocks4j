package com.life4ever.shadowsocks4j.service.local.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life4ever.shadowsocks4j.pojo.Shadowsocks4jDTO;
import com.life4ever.shadowsocks4j.util.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class LocalDataHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        InetSocketAddress targetInetSocketAddress = ChannelUtil.getInetSocketAddress(channelId);
        Shadowsocks4jDTO shadowsocks4jDTO = new Shadowsocks4jDTO(targetInetSocketAddress, msg);

        Channel clientChannel = ctx.channel();
        Channel localChannel;
        if ((localChannel = ChannelUtil.getLocalChannelFromLocalChannelMap(clientChannel)) == null) {
            localChannel = ChannelUtil.createLocalChannel(targetInetSocketAddress.getHostName(), targetInetSocketAddress.getPort());
            ChannelUtil.putLocalChannel(clientChannel, localChannel);
        }

        byte[] shadowsocks4jDTOBytes = MAPPER.writeValueAsBytes(shadowsocks4jDTO);
        localChannel.writeAndFlush(shadowsocks4jDTOBytes);
    }

}
