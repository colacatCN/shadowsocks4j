package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON;

public class Shadowsocks4jProxyFileCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return SHADOWSOCKS4J_PROXY_JSON;
    }

    @Override
    public void resolveCreateEvent() {

    }

    @Override
    public void resolveDeleteEvent() {

    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {

    }

}
