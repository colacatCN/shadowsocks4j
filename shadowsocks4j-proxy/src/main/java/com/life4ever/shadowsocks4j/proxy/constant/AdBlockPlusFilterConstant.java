package com.life4ever.shadowsocks4j.proxy.constant;

import java.util.regex.Pattern;

public class AdBlockPlusFilterConstant {

    public static final Pattern DOMAIN_NAME_PRECISE_PATTERN = Pattern.compile("(^@@\\|https?://)(.*)");

    public static final Pattern DOMAIN_NAME_FUZZY_PATTERN = Pattern.compile("(^@@\\|\\|)(.*)");

    public static final String WHITE_LIST_START_FLAG = "Whitelist Start";

    private AdBlockPlusFilterConstant() {
    }

}
