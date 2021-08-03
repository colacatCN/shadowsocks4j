package com.life4ever.shadowsocks4j.proxy.handler.local;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5InitialRequestHandler.class);

    private Socks5InitialRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        if (!msg.decoderResult().isSuccess()) {
            throw new Shadowsocks4jProxyException("SOCKS protocol version is not 5.");
        }

        Socks5InitialResponse socks5InitialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
        ctx.writeAndFlush(socks5InitialResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.close();
    }

    public static Socks5InitialRequestHandler getInstance() {
        return Socks5InitialRequestHandlerHolder.INSTANCE;
    }

    private static class Socks5InitialRequestHandlerHolder {

        private static final Socks5InitialRequestHandler INSTANCE = new Socks5InitialRequestHandler();

    }

}
