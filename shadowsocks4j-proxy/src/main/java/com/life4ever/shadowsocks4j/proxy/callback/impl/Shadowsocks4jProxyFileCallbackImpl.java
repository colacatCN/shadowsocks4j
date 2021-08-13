package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updateShadowsocks4jProxyConfig;

public class Shadowsocks4jProxyFileCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return SHADOWSOCKS4J_PROXY_JSON;
    }

    @Override
    public void resolveCreateEvent() throws Shadowsocks4jProxyException {
        resolveModifyEvent();
    }

    @Override
    public void resolveDeleteEvent() {
    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {
        updateShadowsocks4jProxyConfig();
    }

}
