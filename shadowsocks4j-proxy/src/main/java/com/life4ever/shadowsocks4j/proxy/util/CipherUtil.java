package com.life4ever.shadowsocks4j.proxy.util;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getCipherFunction;

public class CipherUtil {

    private CipherUtil() {
    }

    public static byte[] encrypt(byte[] content) throws Shadowsocks4jProxyException {
        return getCipherFunction().encrypt(content);
    }

    public static byte[] decrypt(byte[] base64Content) throws Shadowsocks4jProxyException {
        return getCipherFunction().decrypt(base64Content);
    }

}
