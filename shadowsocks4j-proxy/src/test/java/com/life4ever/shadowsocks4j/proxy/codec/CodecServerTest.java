package com.life4ever.shadowsocks4j.proxy.codec;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherDecryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherEncryptHandler;
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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateRemoteMode;

public class CodecServerTest {

    private static final String SERVER_SLOGAN = "Hello Server ";

    private static final Logger LOG = LoggerFactory.getLogger(CodecClientTest.class);

    private final AtomicInteger count = new AtomicInteger(0);

    @Before
    public void before() throws Shadowsocks4jProxyException {
        activateRemoteMode();
    }

    @Test
    public void test() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addFirst(CipherEncryptHandler.getInstance());
                        pipeline.addLast(new CipherDecryptHandler());
                        pipeline.addLast(new CodecServerDataHandler());
                    }
                });

        try {
            ChannelFuture channelFuture = serverBootstrap.bind(10727).sync();
            LOG.info("成功启动 CodecServer");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class CodecServerDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            byte[] bytes = ByteBufUtil.getBytes(msg);
            LOG.info(new String(bytes));
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer((SERVER_SLOGAN + count.getAndIncrement()).getBytes(StandardCharsets.UTF_8)));
        }

    }

}
