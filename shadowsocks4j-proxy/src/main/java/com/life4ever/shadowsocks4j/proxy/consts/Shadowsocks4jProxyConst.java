package com.life4ever.shadowsocks4j.proxy.consts;

public class Shadowsocks4jProxyConst {

    public static final int RUNTIME_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final String SHADOWSOCKS4J_CONF_DIR = System.getProperty("user.dir") + "/conf/";

    public static final String SHADOWSOCKS4J_PROXY_JSON = "shadowsocks4j-proxy.json";

    public static final String SYSTEM_RULE_TXT = "system-rule.txt";

    public static final String USER_RULE_TXT = "user-rule.txt";

    public static final String SHADOWSOCKS4J_PROXY_JSON_LOCATION = SHADOWSOCKS4J_CONF_DIR + SHADOWSOCKS4J_PROXY_JSON;

    public static final String SYSTEM_RULE_TXT_LOCATION = SHADOWSOCKS4J_CONF_DIR + SYSTEM_RULE_TXT;

    public static final String DEFAULT_SYSTEM_RULE_TXT_UPDATER_URL = "https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt";

    public static final Long DEFAULT_SYSTEM_RULE_TXT_UPDATER_INTERVAL = 60 * 60 * 1000L;

    public static final String USER_RULE_TXT_LOCATION = SHADOWSOCKS4J_CONF_DIR + USER_RULE_TXT;

    public static final String FILE_MONITOR_THREAD_NAME = "Shadowsocks4j FileMonitor";

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";

    public static final String DEFAULT_CIPHER_METHOD = "aes_128_gcm";

    public static final String LOCAL_SERVER_SERVICE_NAME = "local-server";

    public static final String REMOTE_SERVER_SERVICE_NAME = "remote-server";

    public static final int IPV4_ADDRESS_BYTE_LENGTH = 4;

    public static final int IPV6_ADDRESS_BYTE_LENGTH = 16;

    public static final String LINE_FEED = "\n";

    public static final String BLANK_STRING = "";

    private Shadowsocks4jProxyConst() {
    }

}
