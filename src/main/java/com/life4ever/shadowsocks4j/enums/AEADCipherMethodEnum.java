package com.life4ever.shadowsocks4j.enums;

import lombok.Getter;

@Getter
public enum AEADCipherMethodEnum {

    AES_CBC_128("AES_CBC_128", 0),

    AES_CBC_256("AES_CBC_256", 1),

    AES_CFB_128("AES_CFB_128", 2),

    AES_CFB_256("AES_CFB_256", 3),

    AES_GCM_128("AES_GCM_128", 4),

    AES_GCM_256("AES_GCM_256", 5),

    ;

    private final String key;

    private final Integer value;

    AEADCipherMethodEnum(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

}
