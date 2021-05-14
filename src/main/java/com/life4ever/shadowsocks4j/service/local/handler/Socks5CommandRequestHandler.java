package com.life4ever.shadowsocks4j.service.local.handler;

import com.life4ever.shadowsocks4j.exception.Shadowsocks4jException;
import com.life4ever.shadowsocks4j.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest socks5CommandRequest) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        if (socks5CommandRequest.decoderResult().isSuccess()) {
            InetSocketAddress targetSocketAddress = new InetSocketAddress(socks5CommandRequest.dstAddr(), socks5CommandRequest.dstPort());
            ChannelUtil.putInetSocketAddress(channelId, targetSocketAddress);
            Socks5CommandResponse socks5CommandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5CommandRequest.dstAddrType());
            ctx.writeAndFlush(socks5CommandResponse);
            log.info("SOCKS5 建立成功");
        } else {
            throw new Shadowsocks4jException("SOCKS5 建立失败");
        }
    }

}
