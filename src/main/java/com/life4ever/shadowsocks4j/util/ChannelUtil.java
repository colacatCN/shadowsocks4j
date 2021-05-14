package com.life4ever.shadowsocks4j.util;

import com.life4ever.shadowsocks4j.service.local.handler.LocalWriteBackHandler;
import com.life4ever.shadowsocks4j.service.server.handler.RemoteWriteBackHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtil {

    /**
     * 根据 client 和 local 之间的 clientChannelId 获取对应的 inetSocketAddress
     */
    private static final Map<String, InetSocketAddress> INET_SOCKET_ADDRESS_MAP = new ConcurrentHashMap<>(128);

    /**
     * 根据 client 和 local 之间的 clientChannel 获取对应的 localChannel
     */
    private static final Map<Channel, Channel> LOCAL_CHANNEL_MAP = new ConcurrentHashMap<>(128);

    /**
     * 根据 local 和 remote 之间的 localChannel 获取对应的 remoteChannel
     */
    private static final Map<Channel, Channel> REMOTE_CHANNEL_MAP = new ConcurrentHashMap<>(128);

    private static final Logger LOG = LoggerFactory.getLogger(ChannelUtil.class);

    public static InetSocketAddress getInetSocketAddress(String channelId) {
        return INET_SOCKET_ADDRESS_MAP.get(channelId);
    }

    public static void putInetSocketAddress(String channelId, InetSocketAddress inetSocketAddress) {
        INET_SOCKET_ADDRESS_MAP.put(channelId, inetSocketAddress);
    }

    public static Channel getLocalChannelFromLocalChannelMap(Channel clientChannel) {
        return LOCAL_CHANNEL_MAP.get(clientChannel);
    }

    public static void putLocalChannel(Channel clientChannel, Channel localChannel) {
        LOCAL_CHANNEL_MAP.put(clientChannel, localChannel);
    }

    public static Channel getRemoteChannel(Channel localChannel) {
        return REMOTE_CHANNEL_MAP.get(localChannel);
    }

    public static Channel getLocalChannelFromRemoteChannelMap(Channel remoteChannel) {
        Channel localChannel = null;
        for (Map.Entry<Channel, Channel> entry : REMOTE_CHANNEL_MAP.entrySet()) {
            if (remoteChannel.equals(entry.getValue())) {
                localChannel = entry.getKey();
            }
        }
        return localChannel;
    }

    public static void putRemoteChannel(Channel localChannel, Channel remoteChannel) {
        REMOTE_CHANNEL_MAP.put(localChannel, remoteChannel);
    }

    public static Channel createLocalChannel(String hostname, int port) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        Channel channel = null;
        try {
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addFirst(new ByteArrayEncoder());
                            pipeline.addLast(new ByteArrayDecoder());
                            pipeline.addLast(new LocalWriteBackHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(hostname, port).sync();
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        }

        return channel;
    }

    public static Channel createRemoteChannel(String hostname, int port) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        Channel channel = null;
        try {
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addFirst(new ByteArrayEncoder());
                            pipeline.addLast(new ByteArrayDecoder());
                            pipeline.addLast(new RemoteWriteBackHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(hostname, port).sync();
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        }

        return channel;
    }

}
