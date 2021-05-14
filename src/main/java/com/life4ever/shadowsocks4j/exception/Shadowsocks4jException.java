package com.life4ever.shadowsocks4j.exception;

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
