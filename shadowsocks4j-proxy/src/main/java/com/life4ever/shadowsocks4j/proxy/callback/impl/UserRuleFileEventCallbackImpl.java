package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.USER_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.USER_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.clearUserRuleWhiteMap;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.lockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.matchDomainName;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.unlockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updateFuzzyDomainNameWhiteSet;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updatePreciseDomainNameWhiteSet;

public class UserRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String fileName() {
        return USER_RULE_TXT;
    }

    @Override
    public void onCreate() throws Shadowsocks4jProxyException {
        onModify();
    }

    @Override
    public void onDelete() {
        clearUserRuleWhiteMap();
    }

    @Override
    public void onModify() throws Shadowsocks4jProxyException {
        lockWhiteList();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(USER_RULE_TXT_LOCATION))) {
            Set<String> preciseDomainNameWhiteList = new HashSet<>(16);
            Set<String> fuzzyDomainNameWhiteList = new HashSet<>(128);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                matchDomainName(line, preciseDomainNameWhiteList, fuzzyDomainNameWhiteList);
            }

            updatePreciseDomainNameWhiteSet(preciseDomainNameWhiteList, false);
            updateFuzzyDomainNameWhiteSet(fuzzyDomainNameWhiteList, false);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        } finally {
            unlockWhiteList();
        }
    }

}
