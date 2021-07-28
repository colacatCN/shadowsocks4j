package com.life4ever.shadowsocks4j.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.util.NetUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class LocalToRemoteHandlerTest {

    @Test
    public void test() {
        Socks5CommandRequest socks5CommandRequest = new DefaultSocks5CommandRequest(
                Socks5CommandType.CONNECT,
                Socks5AddressType.IPv4,
                "127.0.0.1",
                8080);

        parseSocks5CommandRequest(socks5CommandRequest);
    }

    private ByteBuf parseSocks5CommandRequest(Socks5CommandRequest socks5CommandRequest) {
        ByteBuf byteBuf = Unpooled.buffer();

        Socks5AddressType socks5AddressType = socks5CommandRequest.dstAddrType();
        String host = socks5CommandRequest.dstAddr();
        int port = socks5CommandRequest.dstPort();
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
            byteBuf.writeByte(hostnameBytes.length);
            byteBuf.writeBytes(hostnameBytes);
        }

        byteBuf.writeShort(port);
        return byteBuf;
    }

}
