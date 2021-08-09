package com.life4ever.shadowsocks4j.proxy.consts;

import java.util.regex.Pattern;

public class AdBlockPlusFilterConst {

    public static final String DOMAIN_NAME_PRECISE_FLAG = "@@|";

    public static final String DOMAIN_NAME_FUZZY_FLAG = "@@||";

    public static final Pattern DOMAIN_NAME_PRECISE_PATTERN = Pattern.compile("^\\@\\@\\|\\w+");

    public static final Pattern DOMAIN_NAME_FUZZY_PATTERN = Pattern.compile("^\\@\\@\\|\\|\\w+");

    public static final String WHITE_LIST_START_FLAG = "Whitelist Start";

    private AdBlockPlusFilterConst() {
    }

}
