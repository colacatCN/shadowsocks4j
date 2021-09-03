package com.life4ever.shadowsocks4j.proxy.handler.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.encrypt;

@ChannelHandler.Sharable
public class CipherEncryptHandler extends MessageToByteEncoder<ByteBuf> {

    private CipherEncryptHandler() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        byte[] plainContent = ByteBufUtil.getBytes(msg);
        byte[] encryptedContent = encrypt(plainContent);
        out.writeInt(encryptedContent.length);
        out.writeBytes(encryptedContent);
    }

    public static CipherEncryptHandler getInstance() {
        return CipherEncryptHandlerHolder.INSTANCE;
    }

    private static class CipherEncryptHandlerHolder {

        private static final CipherEncryptHandler INSTANCE = new CipherEncryptHandler();

    }

}
