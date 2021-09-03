package com.life4ever.shadowsocks4j.proxy.client;

import com.life4ever.shadowsocks4j.proxy.Shadowsocks4jProxyApplicationTest;
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Ignore
public class Shadowsocks4jClientTest extends Shadowsocks4jProxyApplicationTest {

    private static final String TARGET_SERVER_IP = "127.0.0.1";

    private static final int TARGET_SERVER_PORT = 10728;

    private static final int numOfClient = 1;

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(numOfClient);

    private final SocketAddress localServerSocketAddress = new InetSocketAddress("127.0.0.1", 10418);

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
                            log.info("成功连接 local-server");
                            Socks5InitialRequest socks5InitialRequest = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
                            future.channel().writeAndFlush(socks5InitialRequest);
                        } else {
                            log.error("无法连接 local-server");
                        }
                    });
        }

        TimeUnit.HOURS.sleep(1);
    }

    private class Socks5InitialResponseHandler extends SimpleChannelInboundHandler<Socks5InitialResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Socks5InitialResponse msg) throws Exception {
            Socks5CommandRequest socks5CommandRequest = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.IPv4, TARGET_SERVER_IP, TARGET_SERVER_PORT);
            ctx.writeAndFlush(socks5CommandRequest);
        }

    }

    private class Socks5CommandResponseHandler extends SimpleChannelInboundHandler<Socks5CommandResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Socks5CommandResponse msg) throws Exception {
            String str = "Hello Server";
            ctx.writeAndFlush(Unpooled.copiedBuffer(str.getBytes(StandardCharsets.UTF_8)));
        }

    }

    private class ClientDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            byte[] bytes = ByteBufUtil.getBytes(msg);
            log.info(new String(bytes));
            ctx.writeAndFlush(Unpooled.copiedBuffer(getRandomBytes()));
        }

    }

}
