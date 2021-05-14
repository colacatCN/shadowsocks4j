package com.life4ever.shadowsocks4j.service.local.handler;

import com.life4ever.shadowsocks4j.exception.Shadowsocks4jException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest socks5InitialRequest) throws Exception {
        if (socks5InitialRequest.decoderResult().isSuccess()) {
            List<Socks5AuthMethod> socks5AuthMethods = socks5InitialRequest.authMethods();
            if (socks5AuthMethods.contains(Socks5AuthMethod.NO_AUTH)) {
                Socks5InitialResponse socks5InitialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
                ctx.writeAndFlush(socks5InitialResponse);
            } else {
                throw new Shadowsocks4jException("Socks5InitialRequest 认证失败");
            }
        } else {
            throw new Shadowsocks4jException("Socks5InitialRequest 解码失败");
        }
    }

}
