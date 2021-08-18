package com.life4ever.shadowsocks4j.proxy.constant;

public class ProxyConfigConstant {

    public static final String SHADOWSOCKS4J_CONF_DIR = System.getProperty("user.dir") + "/conf/";

    public static final String SHADOWSOCKS4J_PROXY_JSON = "shadowsocks4j-proxy.json";

    public static final String SYSTEM_RULE_TXT = "system-rule.txt";

    public static final String USER_RULE_TXT = "user-rule.txt";

    public static final String SHADOWSOCKS4J_PROXY_JSON_LOCATION = SHADOWSOCKS4J_CONF_DIR + SHADOWSOCKS4J_PROXY_JSON;

    public static final String SYSTEM_RULE_TXT_LOCATION = SHADOWSOCKS4J_CONF_DIR + SYSTEM_RULE_TXT;

    public static final String USER_RULE_TXT_LOCATION = SHADOWSOCKS4J_CONF_DIR + USER_RULE_TXT;

    public static final String DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL = "https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt";

    public static final long DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL = 60 * 60 * 1000L;

    public static final String DEFAULT_CIPHER_METHOD = "aes_128_gcm";

    private ProxyConfigConstant() {
    }

}
