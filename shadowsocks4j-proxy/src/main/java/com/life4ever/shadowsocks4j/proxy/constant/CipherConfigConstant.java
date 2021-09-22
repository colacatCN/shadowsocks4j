package com.life4ever.shadowsocks4j.proxy.constant;

public class CipherConfigConstant {

    public static final String AES = "AES";

    public static final String CHACHA20 = "ChaCha20";

    public static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static final int SECRET_KEY_ITERATION_COUNT = 65536;

    private CipherConfigConstant() {
    }

}