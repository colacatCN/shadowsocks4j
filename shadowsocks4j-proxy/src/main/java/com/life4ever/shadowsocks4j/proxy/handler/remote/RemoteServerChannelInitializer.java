package com.life4ever.shadowsocks4j.proxy.handler.remote;

import com.life4ever.shadowsocks4j.proxy.handler.common.CommonHeartbeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SERVER_ALL_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SERVER_READ_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SERVER_WRITE_IDLE_TIME;

public class RemoteServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new IdleStateHandler(SERVER_READ_IDLE_TIME, SERVER_WRITE_IDLE_TIME, SERVER_ALL_IDLE_TIME, TimeUnit.MILLISECONDS));
        pipeline.addLast(CommonHeartbeatHandler.getInstance());
        pipeline.addLast(RemoteServerAddressHandler.getInstance());
    }

}
