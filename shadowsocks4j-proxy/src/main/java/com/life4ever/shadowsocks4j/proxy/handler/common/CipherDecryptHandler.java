package com.life4ever.shadowsocks4j.proxy.handler.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.decrypt;

public class CipherDecryptHandler extends ByteToMessageDecoder {

    private static final int HEADER_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (msg.readableBytes() < HEADER_LENGTH) {
            return;
        }

        msg.resetReaderIndex();

        int contentLength = msg.readInt();
        if (msg.readableBytes() < contentLength) {
            msg.resetReaderIndex();
            return;
        }

        byte[] encryptedContent = ByteBufUtil.getBytes(msg, HEADER_LENGTH, contentLength);
        byte[] decryptedContent = decrypt(encryptedContent);
        msg.skipBytes(msg.readableBytes());
        out.add(Unpooled.copiedBuffer(decryptedContent));
    }

}
