package com.life4ever.shadowsocks4j.proxy.handler.remote;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.EXCEPTION_CAUGHT_HANDLER_NAME;
import static com.life4ever.shadowsocks4j.proxy.constant.NettyHandlerConstant.REMOTE_TO_TARGET_HANDLER_NAME;

@ChannelHandler.Sharable
public class RemoteServerAddressHandler extends ChannelInboundHandlerAdapter {

    private static final int IPV4_ADDRESS_BYTES_LENGTH = 4;

    private static final int IPV6_ADDRESS_BYTES_LENGTH = 16;

    private final EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(16);

    private RemoteServerAddressHandler() {
    }

    public static RemoteServerAddressHandler getInstance() {
        return RemoteServerAddressHandlerHolder.INSTANCE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        // 解析
        String host = parseHostString(byteBuf);
        int port = byteBuf.readShort();
        SocketAddress targetServerSocketAddress = new InetSocketAddress(host, port);

        // 交由 RemoteToTargetHandler 处理
        ChannelPipeline pipeline = ctx.channel().pipeline();
        pipeline.addBefore(businessGroup, EXCEPTION_CAUGHT_HANDLER_NAME, REMOTE_TO_TARGET_HANDLER_NAME, new RemoteToTargetHandler(targetServerSocketAddress, ctx));
        pipeline.remove(this);
        ctx.fireChannelRead(byteBuf);
    }

    private String parseHostString(ByteBuf byteBuf) {
        String host;
        byte socks5AddressType = byteBuf.readByte();
        if (Socks5AddressType.IPv4.byteValue() == socks5AddressType) {
            byte[] ipv4Bytes = new byte[IPV4_ADDRESS_BYTES_LENGTH];
            byteBuf.readBytes(ipv4Bytes);
            host = NetUtil.bytesToIpAddress(ipv4Bytes);
        } else if (Socks5AddressType.IPv6.byteValue() == socks5AddressType) {
            byte[] ipv6Bytes = new byte[IPV6_ADDRESS_BYTES_LENGTH];
            byteBuf.readBytes(ipv6Bytes);
            host = NetUtil.bytesToIpAddress(ipv6Bytes);
        } else {
            int hostnameLength = byteBuf.readShort();
            byte[] hostnameBytes = new byte[hostnameLength];
            byteBuf.readBytes(hostnameBytes);
            host = new String(hostnameBytes);
        }
        return host;
    }

    private static class RemoteServerAddressHandlerHolder {

        private static final RemoteServerAddressHandler INSTANCE = new RemoteServerAddressHandler();

    }

}
