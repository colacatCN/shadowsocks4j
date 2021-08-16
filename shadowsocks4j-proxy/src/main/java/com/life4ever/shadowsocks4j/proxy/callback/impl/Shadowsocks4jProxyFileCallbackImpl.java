package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SHADOWSOCKS4J_PROXY_JSON;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updateShadowsocks4jProxyConfig;

public class Shadowsocks4jProxyFileCallbackImpl implements FileEventCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Shadowsocks4jProxyFileCallbackImpl.class);

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
        LOG.warn("The configuration file has been deleted.");
    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {
        updateShadowsocks4jProxyConfig();
    }

}
