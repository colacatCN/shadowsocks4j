package com.life4ever.shadowsocks4j.proxy.handler.remote;

import com.life4ever.shadowsocks4j.proxy.handler.common.CipherDecryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherEncryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.HeartbeatTimeoutHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_ALL_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_READ_IDLE_TIME;
import static com.life4ever.shadowsocks4j.proxy.constant.IdleTimeConstant.SERVER_WRITE_IDLE_TIME;

public class RemoteServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final EventLoopGroup clientWorkerGroup;

    public RemoteServerChannelInitializer(EventLoopGroup clientWorkerGroup) {
        this.clientWorkerGroup = clientWorkerGroup;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst(CipherEncryptHandler.getInstance());
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(CipherDecryptHandler.getInstance());
        pipeline.addLast(new IdleStateHandler(SERVER_READ_IDLE_TIME, SERVER_WRITE_IDLE_TIME, SERVER_ALL_IDLE_TIME, TimeUnit.MILLISECONDS));
        pipeline.addLast(HeartbeatTimeoutHandler.getInstance());
        pipeline.addLast(RemoteServerAddressHandler.getInstance(clientWorkerGroup));
    }

}
