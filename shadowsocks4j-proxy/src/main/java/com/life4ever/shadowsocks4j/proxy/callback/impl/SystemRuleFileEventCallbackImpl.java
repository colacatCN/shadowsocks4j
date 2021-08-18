package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.life4ever.shadowsocks4j.proxy.constant.AdBlockPlusFilterConstant.WHITE_LIST_START_FLAG;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.SYSTEM_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.constant.ProxyConfigConstant.SYSTEM_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.clearSystemRuleWhiteMap;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.lockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.matchDomainName;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.unlockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updateFuzzyDomainNameWhiteSet;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updatePreciseDomainNameWhiteSet;

public class SystemRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return SYSTEM_RULE_TXT;
    }

    @Override
    public void onCreate() throws Shadowsocks4jProxyException {
        onModify();
    }

    @Override
    public void onDelete() {
        clearSystemRuleWhiteMap();
    }

    @Override
    public void onModify() throws Shadowsocks4jProxyException {
        lockWhiteList();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(SYSTEM_RULE_TXT_LOCATION))) {
            Set<String> preciseDomainNameWhiteList = new HashSet<>(16);
            Set<String> fuzzyDomainNameWhiteList = new HashSet<>(128);

            String line;
            boolean whiteListStart = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(WHITE_LIST_START_FLAG)) {
                    whiteListStart = true;
                }
                if (whiteListStart) {
                    matchDomainName(line, preciseDomainNameWhiteList, fuzzyDomainNameWhiteList);
                }
            }

            updatePreciseDomainNameWhiteSet(preciseDomainNameWhiteList, true);
            updateFuzzyDomainNameWhiteSet(fuzzyDomainNameWhiteList, true);
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        } finally {
            unlockWhiteList();
        }
    }

}
