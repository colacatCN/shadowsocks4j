package com.life4ever.shadowsocks4j.proxy.handler.common;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

@ChannelHandler.Sharable
public class HeartbeatTimeoutHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatTimeoutHandler.class);

    private HeartbeatTimeoutHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (IdleState.READER_IDLE.equals(idleStateEvent.state())) {
                SocketAddress socketAddress = ctx.channel().remoteAddress();
                LOG.info("Close channel @ {}.", socketAddress);
                ctx.channel().close();
            }
        }
    }

    public static HeartbeatTimeoutHandler getInstance() {
        return HeartbeatTimeoutHandlerHolder.INSTANCE;
    }

    private static class HeartbeatTimeoutHandlerHolder {

        private static final HeartbeatTimeoutHandler INSTANCE = new HeartbeatTimeoutHandler();

    }

}
