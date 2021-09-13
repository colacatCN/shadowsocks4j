package com.life4ever.shadowsocks4j.proxy.enums;

public enum MatcherModeEnum {

    PRECISE("precise-mode", "精确匹配"),

    FUZZY("fuzzy-mode", "模糊匹配"),

    ;

    private final String key;

    private final String description;

    MatcherModeEnum(String key, String description) {
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
