package com.life4ever.shadowsocks4j.proxy.enums;

public enum CipherAlgorithmEnum {

    AES("aes", "AES/GCM/NoPadding"),

    CHACHA20("chacha20", "ChaCha20-Poly1305/None/NoPadding"),

    ;

    private final String name;

    private final String mode;

    CipherAlgorithmEnum(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public String getMode() {
        return mode;
    }

}
