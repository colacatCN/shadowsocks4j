package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.USER_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.clearUserRuleWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getUserRuleFuzzyDomainNameWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getUserRulePreciseDomainNameWhiteList;

public class UserRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return USER_RULE_TXT;
    }

    @Override
    public void resolveCreateEvent() throws Shadowsocks4jProxyException {
        resolveModifyEvent();
    }

    @Override
    public void resolveDeleteEvent() {
        clearUserRuleWhiteList();
    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {
        clearUserRuleWhiteList();
        List<String> preciseDomainNameWhiteList = getUserRulePreciseDomainNameWhiteList();
        List<String> fuzzyDomainNameWhiteList = getUserRuleFuzzyDomainNameWhiteList();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(USER_RULE_TXT_LOCATION))) {
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
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

}
