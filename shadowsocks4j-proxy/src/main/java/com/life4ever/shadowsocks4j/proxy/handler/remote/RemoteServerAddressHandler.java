package com.life4ever.shadowsocks4j.proxy.handler.remote;

import com.life4ever.shadowsocks4j.proxy.handler.common.ExceptionCaughtHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.decrypt;

@ChannelHandler.Sharable
public class RemoteServerAddressHandler extends ChannelInboundHandlerAdapter {

    private static final int IPV4_ADDRESS_BYTES_LENGTH = 4;

    private static final int IPV6_ADDRESS_BYTES_LENGTH = 16;

    private static volatile RemoteServerAddressHandler instance;

    private final EventLoopGroup clientWorkerGroup;

    private RemoteServerAddressHandler(EventLoopGroup clientWorkerGroup) {
        this.clientWorkerGroup = clientWorkerGroup;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        // 解密
        byte[] decryptedBytes = decrypt(ByteBufUtil.getBytes(byteBuf));
        ReferenceCountUtil.release(msg);

        // 解析
        ByteBuf decryptedByteBuf = Unpooled.buffer();
        decryptedByteBuf.writeBytes(decryptedBytes);
        String host = parseHostString(decryptedByteBuf);
        int port = decryptedByteBuf.readShort();

        // 交由 RemoteToTargetHandler 处理
        ChannelPipeline pipeline = ctx.channel().pipeline();
        pipeline.addLast(new RemoteToTargetHandler(clientWorkerGroup, new InetSocketAddress(host, port), ctx));
        pipeline.addLast(ExceptionCaughtHandler.getInstance());
        pipeline.remove(this);
        ctx.fireChannelRead(decryptedByteBuf);
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

    public static RemoteServerAddressHandler getInstance(EventLoopGroup clientWorkerGroup) {
        if (instance == null) {
            synchronized (RemoteServerAddressHandler.class) {
                if (instance == null) {
                    instance = new RemoteServerAddressHandler(clientWorkerGroup);
                }
            }
        }
        return instance;
    }

}
