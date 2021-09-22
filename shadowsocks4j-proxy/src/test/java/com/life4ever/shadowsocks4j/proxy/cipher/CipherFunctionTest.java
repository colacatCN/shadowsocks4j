package com.life4ever.shadowsocks4j.proxy.cipher;

import com.life4ever.shadowsocks4j.proxy.cipher.impl.AesCipherFunctionImpl;
import com.life4ever.shadowsocks4j.proxy.cipher.impl.Chacha20CipherFunctionImpl;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static com.life4ever.shadowsocks4j.proxy.enums.CipherMethodEnum.AES_128_GCM;
import static com.life4ever.shadowsocks4j.proxy.enums.CipherMethodEnum.CHACHA20_POLY1305;

public class CipherFunctionTest {

    private static final String PASSWORD = "123456";

    private static final String SALT = "654321";

    private static final byte[] PLAIN_CONTENT = "Hello World!".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testAes() throws Shadowsocks4jProxyException {
        ICipherFunction cipherFunction = new AesCipherFunctionImpl(PASSWORD, SALT, AES_128_GCM.getSecretKeyLength(), AES_128_GCM.getMode());
        byte[] encrypted = cipherFunction.encrypt(PLAIN_CONTENT);
        byte[] decrypted = cipherFunction.decrypt(encrypted);
        Assert.assertArrayEquals(PLAIN_CONTENT, decrypted);
    }

    @Test
    public void testChacha20() throws Shadowsocks4jProxyException {
        ICipherFunction cipherFunction = new Chacha20CipherFunctionImpl(PASSWORD, SALT, CHACHA20_POLY1305.getSecretKeyLength(), CHACHA20_POLY1305.getMode());
        byte[] encrypted = cipherFunction.encrypt(PLAIN_CONTENT);
        byte[] decrypted = cipherFunction.decrypt(encrypted);
        Assert.assertArrayEquals(PLAIN_CONTENT, decrypted);
    }

}
