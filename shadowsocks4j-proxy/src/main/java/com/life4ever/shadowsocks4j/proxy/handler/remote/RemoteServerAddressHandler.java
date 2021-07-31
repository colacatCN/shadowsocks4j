package com.life4ever.shadowsocks4j.proxy.handler.remote;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.NetUtil;

import java.net.InetSocketAddress;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.IPV4_ADDRESS_BYTE_LENGTH;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.IPV6_ADDRESS_BYTE_LENGTH;
import static com.life4ever.shadowsocks4j.proxy.util.CryptoUtil.decrypt;

public class RemoteServerAddressHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        // 解密
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byte[] decryptedBytes = decrypt(bytes);

        // 解析
        ByteBuf decryptedByteBuf = Unpooled.buffer();
        decryptedByteBuf.writeBytes(decryptedBytes);
        String host = parseHost(decryptedByteBuf);
        int port = decryptedByteBuf.readShort();

        // 交由 RemoteToTargetHandler 处理
        ctx.channel().pipeline().addLast(new RemoteToTargetHandler(new InetSocketAddress(host, port), ctx));
        ctx.channel().pipeline().remove(this);
        ctx.fireChannelRead(decryptedByteBuf);
    }

    private String parseHost(ByteBuf byteBuf) {
        String host;
        byte socks5AddressType = byteBuf.readByte();
        if (Socks5AddressType.IPv4.byteValue() == socks5AddressType) {
            byte[] ipv4Bytes = new byte[IPV4_ADDRESS_BYTE_LENGTH];
            byteBuf.readBytes(ipv4Bytes);
            host = NetUtil.bytesToIpAddress(ipv4Bytes);
        } else if (Socks5AddressType.IPv6.byteValue() == socks5AddressType) {
            byte[] ipv6Bytes = new byte[IPV6_ADDRESS_BYTE_LENGTH];
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

}
