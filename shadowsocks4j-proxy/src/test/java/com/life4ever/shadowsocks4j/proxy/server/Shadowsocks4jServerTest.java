package com.life4ever.shadowsocks4j.proxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.LOOPBACK_ADDRESS;

@Ignore
public class Shadowsocks4jServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(Shadowsocks4jServerTest.class);

    private final AtomicInteger count = new AtomicInteger(1);

    @Test
    public void test() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                byte[] bytes = ByteBufUtil.getBytes(msg);
                                LOG.info(new String(bytes));
                                ctx.writeAndFlush(Unpooled.copiedBuffer(("Hello Client " + count.getAndAdd(2)).getBytes(StandardCharsets.UTF_8)));
                            }

                        });
                    }

                });

        try {
            ChannelFuture channelFuture = serverBootstrap.bind(LOOPBACK_ADDRESS, 10728).sync();
            LOG.info("成功启动 Shadowsocks4jServer");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("无法启动 Shadowsocks4jServer");
            LOG.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
