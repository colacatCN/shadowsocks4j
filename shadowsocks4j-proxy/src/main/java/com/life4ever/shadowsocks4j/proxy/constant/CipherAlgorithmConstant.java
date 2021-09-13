package com.life4ever.shadowsocks4j.proxy.constant;

import java.util.regex.Pattern;

public class CipherAlgorithmConstant {

    public static final String DEFAULT_CIPHER_METHOD = "aes-128-gcm";

    public static final Pattern SECRET_KEY_LENGTH_PATTERN = Pattern.compile("\\d{3}");

    public static final int DEFAULT_SECRET_KEY_LENGTH = 128;

    public static final int MAXIMUM_SECRET_KEY_LENGTH = 256;

    public static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static final int SECRET_KEY_ITERATION_COUNT = 65536;

    private CipherAlgorithmConstant() {
    }

}
