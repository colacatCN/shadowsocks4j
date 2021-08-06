package com.life4ever.shadowsocks4j.proxy.handler.local;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

@ChannelHandler.Sharable
public class LocalHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(LocalHeartbeatHandler.class);

    private LocalHeartbeatHandler() {
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

    public static LocalHeartbeatHandler getInstance() {
        return LocalHeartbeatHandlerHolder.INSTANCE;
    }

    private static class LocalHeartbeatHandlerHolder {

        private static final LocalHeartbeatHandler INSTANCE = new LocalHeartbeatHandler();

    }

}
