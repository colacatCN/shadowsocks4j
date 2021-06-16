package com.life4ever.shadowsocks4j.exception;

/**
 * @author zhouke
 * @date 2021/06/16
 */
public class Shadowsocks4jException extends Exception {

    public Shadowsocks4jException(String message) {
        super(message);
    }

    public Shadowsocks4jException(String message, Throwable cause) {
        super(message, cause);
    }

    public Shadowsocks4jException(Throwable cause) {
        super(cause);
    }

}
