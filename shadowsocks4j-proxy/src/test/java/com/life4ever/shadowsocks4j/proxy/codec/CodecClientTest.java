package com.life4ever.shadowsocks4j.proxy.codec;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherDecryptHandler;
import com.life4ever.shadowsocks4j.proxy.handler.common.CipherEncryptHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateRemoteMode;

public class CodecClientTest {

    private static final String CLIENT_SLOGAN = "Hello Server";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Logger LOG = LoggerFactory.getLogger(CodecClientTest.class);

    @Before
    public void before() throws Shadowsocks4jProxyException {
        activateRemoteMode();
    }

    @Test
    public void test() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addFirst(CipherEncryptHandler.getInstance());
                        pipeline.addLast(new CipherDecryptHandler());
                        pipeline.addLast(new CodecClientDataHandler());
                    }
                });

        bootstrap.connect("127.0.0.1", 10727)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        LOG.info("成功连接 CodecServer");
                        future.channel().writeAndFlush(Unpooled.copiedBuffer(CLIENT_SLOGAN.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        LOG.error("无法连接 CodecServer");
                    }
                });

        TimeUnit.HOURS.sleep(1);
    }

    private class CodecClientDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            byte[] bytes = ByteBufUtil.getBytes(msg);
            LOG.info(new String(bytes));
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(getRandomBytes()));
        }

    }

    private byte[] getRandomBytes() {
        byte[] bytes = new byte[1024];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

}
