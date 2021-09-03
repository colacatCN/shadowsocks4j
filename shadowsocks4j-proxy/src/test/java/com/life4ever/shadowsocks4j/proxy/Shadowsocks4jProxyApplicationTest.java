package com.life4ever.shadowsocks4j.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class Shadowsocks4jProxyApplicationTest {

    private static final int MAX_BYTES_LENGTH = 4096;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static byte[] getRandomBytes() {
        int length = SECURE_RANDOM.nextInt(MAX_BYTES_LENGTH);
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

}
