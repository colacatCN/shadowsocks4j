package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT;

public class UserRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return USER_RULE_TXT;
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
