package com.life4ever.shadowsocks4j.proxy.constant;

import java.util.regex.Pattern;

public class AEADCipherAlgorithmConstant {

    public static final String AES = "aes";

    public static final String CHACHA20 = "chacha20";

    public static final String DEFAULT_CIPHER_METHOD = "aes-128-gcm";

    public static final int MAX_SECRET_KEY_LENGTH = 256;

    public static final Pattern SECRET_KEY_LENGTH_PATTERN = Pattern.compile("\\d{3}");

    private AEADCipherAlgorithmConstant() {
    }

}
