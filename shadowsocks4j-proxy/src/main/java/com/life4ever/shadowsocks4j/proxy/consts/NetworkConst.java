package com.life4ever.shadowsocks4j.proxy.consts;

public class NetworkConst {

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";

    public static final int IPV4_ADDRESS_BYTE_LENGTH = 4;

    public static final int IPV6_ADDRESS_BYTE_LENGTH = 16;

    public static final long SERVER_READ_IDLE_TIME = 5 * 60 * 1000L;

    public static final long SERVER_WRITE_IDLE_TIME = 5 * 60 * 1000L;

    public static final long SERVER_ALL_IDLE_TIME = 5 * 60 * 1000L;

    public static final long HTTP_CLIENT_CONNECT_TIMEOUT = 60 * 1000L;

    public static final long HTTP_CLIENT_READ_TIMEOUT = 60 * 1000L;

    public static final long HTTP_CLIENT_WRITE_TIMEOUT = 60 * 1000L;

    private NetworkConst() {
    }

}
