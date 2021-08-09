package com.life4ever.shadowsocks4j.proxy.enums;

public enum ShadowsocksProxyModeEnum {

    LOCAL("local-server", 0),

    REMOTE("remote-server", 1),

    REVERSE("reverse-server", 2),

    ;

    private final String key;

    private final Integer value;

    ShadowsocksProxyModeEnum(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

}
