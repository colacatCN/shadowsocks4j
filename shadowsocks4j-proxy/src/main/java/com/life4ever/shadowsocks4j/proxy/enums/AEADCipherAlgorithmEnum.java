package com.life4ever.shadowsocks4j.proxy.enums;

public enum AEADCipherAlgorithmEnum {

    AES_128_GCM("AES", 128, "AES/GCM/NoPadding"),

    AES_256_GCM("AES", 256, "AES/GCM/NoPadding"),

    CHACHA20_POLY1305("CHACHA20", 256, "ChaCha20-poly1305"),

    ;

    private String name;

    private Integer keyLength;

    private String mode;

    AEADCipherAlgorithmEnum(String name, Integer keyLength, String mode) {
        this.name = name;
        this.keyLength = keyLength;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public Integer getKeyLength() {
        return keyLength;
    }

    public String getMode() {
        return mode;
    }

}
