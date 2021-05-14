package com.life4ever.shadowsocks4j.service.server;

import com.life4ever.shadowsocks4j.service.IShadowsocks4jService;
import com.life4ever.shadowsocks4j.service.server.handler.ServerDataHandler;
import com.life4ever.shadowsocks4j.util.ConfigUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author leo
 */
@Slf4j
public class Shadowsocks4jServerServiceImpl implements IShadowsocks4jService {

    @Override
    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addFirst(new ByteArrayEncoder());
                            pipeline.addLast(new ByteArrayDecoder());
                            pipeline.addLast(new ServerDataHandler());
                        }
                    });

            int serverPort = ConfigUtil.getServerConfig().getPort();
            ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
