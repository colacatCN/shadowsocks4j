package com.life4ever.shadowsocks4j.proxy.enums;

public enum ShadowsocksProxyModeEnum {

    LOCAL("local-server", "本地代理"),

    REMOTE("remote-server", "远程代理"),

    REVERSE("reverse-server", "反向代理"),

    ;

    private final String key;

    private final String description;

    ShadowsocksProxyModeEnum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

}
