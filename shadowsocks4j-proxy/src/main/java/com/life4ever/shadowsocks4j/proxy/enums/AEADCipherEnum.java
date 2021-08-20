package com.life4ever.shadowsocks4j.proxy.enums;

public enum AEADCipherEnum {

    AES_128_GCM("aes-128-gcm", "AES/GCM/NoPadding"),

    AES_256_GCM("aes-256-gcm", "AES/GCM/NoPadding"),

    CHACHA20_POLY1305("chacha20-ietf-poly1305", "ChaCha20-Poly1305"),

    ;

    private final String key;

    private final String value;

    AEADCipherEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
