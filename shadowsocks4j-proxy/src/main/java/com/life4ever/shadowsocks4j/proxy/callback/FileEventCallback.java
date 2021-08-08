package com.life4ever.shadowsocks4j.proxy.callback;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

public interface FileEventCallback {

    String getFileName();

    void resolveCreateEvent() throws Shadowsocks4jProxyException;

    void resolveDeleteEvent() throws Shadowsocks4jProxyException;

    void resolveModifyEvent() throws Shadowsocks4jProxyException;

}
