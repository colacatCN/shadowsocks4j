package com.life4ever.shadowsocks4j.proxy.callback;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

public interface FileEventCallback {

    String fileName();

    void onCreate() throws Shadowsocks4jProxyException;

    void onDelete() throws Shadowsocks4jProxyException;

    void onModify() throws Shadowsocks4jProxyException;

}
