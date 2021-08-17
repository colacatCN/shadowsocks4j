package com.life4ever.shadowsocks4j.proxy.client;

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
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Ignore
public class Shadowsocks4jClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(Shadowsocks4jClientTest.class);

    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private static final String TARGET_SERVER_IP = "10.80.12.82";

    private static final int TARGET_SERVER_PORT = 10728;

    private static final int numOfClient = 100;

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(numOfClient);

    private final SocketAddress localServerSocketAddress = new InetSocketAddress("10.80.12.6", 10418);

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < numOfClient; i++) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            // encoder
                            pipeline.addFirst(Socks5ClientEncoder.DEFAULT);

                            // init
                            pipeline.addLast(new Socks5InitialResponseDecoder());
                            pipeline.addLast(new Socks5InitialResponseHandler());

                            // command
                            pipeline.addLast(new Socks5CommandResponseDecoder());
                            pipeline.addLast(new Socks5CommandResponseHandler());

                            // data
                            pipeline.addLast(new ClientDataHandler());
                        }

                    });

            bootstrap.connect(localServerSocketAddress)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            LOG.info("成功连接 local-server");
                            Socks5InitialRequest socks5InitialRequest = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
                            future.channel().writeAndFlush(socks5InitialRequest);
                        } else {
                            LOG.error("无法连接 local-server");
                        }
                    });
        }

        TimeUnit.HOURS.sleep(1);
    }

    private static class Socks5InitialResponseHandler extends SimpleChannelInboundHandler<Socks5InitialResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Socks5InitialResponse msg) throws Exception {
            Socks5CommandRequest socks5CommandRequest = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.IPv4, TARGET_SERVER_IP, TARGET_SERVER_PORT);
            ctx.writeAndFlush(socks5CommandRequest);
        }

    }

    private static class Socks5CommandResponseHandler extends SimpleChannelInboundHandler<Socks5CommandResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Socks5CommandResponse msg) throws Exception {
            String str = "Hello Server";
            ctx.writeAndFlush(Unpooled.copiedBuffer(str.getBytes(StandardCharsets.UTF_8)));
        }

    }

    private static class ClientDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            byte[] bytes = ByteBufUtil.getBytes(msg);
            LOG.info(new String(bytes));
            ctx.writeAndFlush(Unpooled.copiedBuffer(getRandomBytes()));
        }

    }

    private static byte[] getRandomBytes() {
        byte[] bytes = new byte[1024];
        THREAD_LOCAL_RANDOM.nextBytes(bytes);
        return bytes;
    }

}
