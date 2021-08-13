package com.life4ever.shadowsocks4j.proxy.handler.common;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionCaughtHandler.class);

    private ExceptionCaughtHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.channel().close();
    }

    public static ExceptionCaughtHandler getInstance() {
        return ExceptionCaughtHandlerHolder.INSTANCE;
    }

    private static class ExceptionCaughtHandlerHolder {

        private static final ExceptionCaughtHandler INSTANCE = new ExceptionCaughtHandler();

    }

}
