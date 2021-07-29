package com.life4ever.shadowsocks4j.handler.local;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LocalToRemoteHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(LocalToRemoteHandler.class);

    private final ChannelFuture localServerChannelFuture;

    private final Socks5CommandRequest socks5CommandRequest;

    private boolean isInitOk = false;

    public LocalToRemoteHandler(ChannelFuture localServerChannelFuture, Socks5CommandRequest socks5CommandRequest) {
        this.localServerChannelFuture = localServerChannelFuture;
        this.socks5CommandRequest = socks5CommandRequest;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (!isInitOk) {
            ByteBuf byteBufWithTargetAddress = parseSocks5CommandRequest(socks5CommandRequest);
            byteBufWithTargetAddress.writeBytes(byteBuf);
            byteBuf = byteBufWithTargetAddress;
            isInitOk = true;
        }
        localServerChannelFuture.channel().writeAndFlush(byteBuf);
    }

    private ByteBuf parseSocks5CommandRequest(Socks5CommandRequest socks5CommandRequest) {
        Socks5AddressType socks5AddressType = socks5CommandRequest.dstAddrType();
        String host = socks5CommandRequest.dstAddr();
        int port = socks5CommandRequest.dstPort();
        LOG.info("socks5AddressType = {}, host = {}, port = {}", socks5AddressType, host, port);

        ByteBuf byteBuf = Unpooled.buffer();
        if (Socks5AddressType.IPv4.equals(socks5AddressType)) {
            byteBuf.writeByte(0x01);
            byte[] ipv4AddressBytes = NetUtil.createByteArrayFromIpAddressString(host);
            byteBuf.writeBytes(ipv4AddressBytes);
        } else if (Socks5AddressType.IPv6.equals(socks5AddressType)) {
            byteBuf.writeByte(0x04);
            byte[] ipv6AddressBytes = NetUtil.createByteArrayFromIpAddressString(host);
            byteBuf.writeBytes(ipv6AddressBytes);
        } else {
            byteBuf.writeByte(0x03);
            byte[] hostnameBytes = host.getBytes(StandardCharsets.UTF_8);
            byteBuf.writeShort(hostnameBytes.length);
            byteBuf.writeBytes(hostnameBytes);
        }
        byteBuf.writeShort(port);
        return byteBuf;
    }

}
