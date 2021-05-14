package com.life4ever.shadowsocks4j.service.local;

import com.life4ever.shadowsocks4j.service.IShadowsocks4jService;
import com.life4ever.shadowsocks4j.service.local.handler.LocalDataHandler;
import com.life4ever.shadowsocks4j.service.local.handler.Socks5CommandRequestHandler;
import com.life4ever.shadowsocks4j.service.local.handler.Socks5InitialRequestHandler;
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
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author leo
 */
@Slf4j
public class Shadowsocks4jLocalServiceImpl implements IShadowsocks4jService {

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

                            // 编码器
                            pipeline.addFirst(Socks5ServerEncoder.DEFAULT);
                            pipeline.addFirst(new ByteArrayEncoder());

                            // 1. 解码处理 Socks5InitialRequest
                            pipeline.addLast(new Socks5InitialRequestDecoder());
                            pipeline.addLast(new Socks5InitialRequestHandler());

                            // 2. 解码处理 Socks5CommandRequest
                            pipeline.addLast(new Socks5CommandRequestDecoder());
                            pipeline.addLast(new Socks5CommandRequestHandler());

                            // 3. 解码处理 Data
                            pipeline.addLast(new ByteArrayDecoder());
                            pipeline.addLast(new LocalDataHandler());
                        }
                    });

            int localPort = ConfigUtil.getLocalConfig().getPort();
            ChannelFuture channelFuture = serverBootstrap.bind(localPort).sync();
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
