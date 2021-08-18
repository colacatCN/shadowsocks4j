package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.clearUserRuleWhiteMap;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.lockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.unlockWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updateFuzzyDomainNameWhiteSet;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.updatePreciseDomainNameWhiteSet;

public class UserRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
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
            String domainName;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = DOMAIN_NAME_PRECISE_PATTERN.matcher(line);
                if (matcher.find() && (domainName = matcher.group(2)) != null) {
                    preciseDomainNameWhiteList.add(domainName);
                    continue;
                }
                matcher = DOMAIN_NAME_FUZZY_PATTERN.matcher(line);
                if (matcher.find() && (domainName = matcher.group(2)) != null) {
                    fuzzyDomainNameWhiteList.add(domainName);
                }
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
