package com.life4ever.shadowsocks4j.service.local;

import com.life4ever.shadowsocks4j.config.Shadowsocks4jLocalConfig;
import com.life4ever.shadowsocks4j.service.IShadowsocks4jService;
import com.life4ever.shadowsocks4j.service.local.handler.Shadowsocks4jLocalChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhouke
 * @date 2021/06/16
 */
@Slf4j
public class Shadowsocks4jLocalServiceImpl implements IShadowsocks4jService {

    @Autowired
    private Shadowsocks4jLocalConfig localConfig;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    @Override
    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new Shadowsocks4jLocalChannelInitializer());

            int port = localConfig.getPort();
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("Shadowsocks4jLocalServiceImpl 已启动, 端口号: {}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
