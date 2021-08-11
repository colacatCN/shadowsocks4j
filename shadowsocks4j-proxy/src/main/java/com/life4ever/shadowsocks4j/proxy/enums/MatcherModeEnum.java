package com.life4ever.shadowsocks4j.proxy.enums;

public enum MatcherModeEnum {

    PRECISE("precise-mode", 0),

    FUZZY("fuzzy-mode", 1),

    ;

    private final String key;

    private final Integer value;

    MatcherModeEnum(String key, Integer value) {
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
