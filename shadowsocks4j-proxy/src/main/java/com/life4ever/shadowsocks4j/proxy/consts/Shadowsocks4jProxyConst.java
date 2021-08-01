package com.life4ever.shadowsocks4j.proxy.consts;

public class Shadowsocks4jProxyConst {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String APPLICATION_CONFIG_FILE_NAME = "shadowsocks4j-proxy.yml";

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";

    public static final String DEFAULT_CIPHER_METHOD = "aes_128_gcm";

    public static final int IPV4_ADDRESS_BYTE_LENGTH = 4;

    public static final int IPV6_ADDRESS_BYTE_LENGTH = 32;

    private Shadowsocks4jProxyConst() {
    }

}
