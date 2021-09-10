package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.decrypt;
import static com.life4ever.shadowsocks4j.proxy.util.CipherUtil.encrypt;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateRemoteMode;

@Ignore
public class CipherUtilTest {

    @Before
    public void before() throws Shadowsocks4jProxyException {
        activateRemoteMode();
    }

    @Test
    public void test() throws Shadowsocks4jProxyException {
        String str = "Hello World!";
        byte[] plain = str.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = encrypt(plain);
        System.out.println(Arrays.toString(encrypted));

        byte[] decrypted = decrypt(encrypted);
        System.out.println(Arrays.toString(decrypted));

        Assert.assertArrayEquals(plain, decrypted);
    }

}
