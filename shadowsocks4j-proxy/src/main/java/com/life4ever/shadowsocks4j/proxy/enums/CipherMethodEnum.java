package com.life4ever.shadowsocks4j.proxy.enums;

public enum CipherMethodEnum {

    AES_128_GCM("AES", 128, "AES/GCM/NoPadding"),

    AES_256_GCM("AES", 256, "AES/GCM/NoPadding"),

    AES_128_CFB("AES", 128, "AES/CFB/NoPadding"),

    AES_256_CFB("AES", 256, "AES/CFB/NoPadding"),

    CHACHA20_POLY1305("ChaCha20", 256, "ChaCha20-Poly1305/None/NoPadding"),

    ;

    private final String algorithm;

    private final Integer secretKeyLength;

    private final String mode;

    CipherMethodEnum(String algorithm, Integer secretKeyLength, String mode) {
        this.algorithm = algorithm;
        this.secretKeyLength = secretKeyLength;
        this.mode = mode;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Integer getSecretKeyLength() {
        return secretKeyLength;
    }

    public String getMode() {
        return mode;
    }

}
