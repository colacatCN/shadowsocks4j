package com.life4ever.shadowsocks4j.proxy.constant;

import java.util.regex.Pattern;

public class CipherAlgorithmConstant {

    public static final String DEFAULT_AES_METHOD = "aes-128-gcm";

    public static final String DEFAULT_CHACHA20_METHOD = "chacha20-poly1305";

    public static final int DEFAULT_SECRET_KEY_LENGTH = 128;

    public static final Pattern SECRET_KEY_LENGTH_PATTERN = Pattern.compile("\\d{3}");

    private CipherAlgorithmConstant() {
    }

}
