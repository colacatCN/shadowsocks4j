package com.life4ever.shadowsocks4j.proxy.handler.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.decrypt;

@ChannelHandler.Sharable
public class CipherDecryptHandler extends ChannelInboundHandlerAdapter {

    private CipherDecryptHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] encryptedContent = ByteBufUtil.getBytes(byteBuf);
        byte[] decryptedContent = decrypt(encryptedContent);
        ReferenceCountUtil.release(byteBuf);
        ctx.fireChannelRead(Unpooled.copiedBuffer(decryptedContent));
    }

    public static CipherDecryptHandler getInstance() {
        return CipherDecryptHandlerHolder.INSTANCE;
    }

    private static class CipherDecryptHandlerHolder {

        private static final CipherDecryptHandler INSTANCE = new CipherDecryptHandler();

    }

}
