package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_MATCHER;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_MATCHER;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.FUZZY_MATCHER_REGEX_EXPRESSION;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.PRECISE_MATCHER_REGEX_EXPRESSION;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.WHITE_LIST_START_FLAG;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SYSTEM_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getSystemRuleFuzzyDomainNameWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getSystemRuleLocation;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getSystemRulePreciseDomainNameWhiteList;

public class SystemRuleFileEventCallbackImpl implements FileEventCallback {

    private static final Pattern PRECISE_MATCHER_PATTERN = Pattern.compile(PRECISE_MATCHER_REGEX_EXPRESSION);

    private static final Pattern FUZZY_MATCHER_PATTERN = Pattern.compile(FUZZY_MATCHER_REGEX_EXPRESSION);

    @Override
    public String getFileName() {
        return SYSTEM_RULE_TXT;
    }

    @Override
    public void resolveCreateEvent() {

    }

    @Override
    public void resolveDeleteEvent() {

    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {
        String systemRuleLocation = getSystemRuleLocation();

        // 清空白名单
        List<String> preciseDomainNameWhiteList = getSystemRulePreciseDomainNameWhiteList();
        preciseDomainNameWhiteList.clear();
        List<String> fuzzyDomainNameWhiteList = getSystemRuleFuzzyDomainNameWhiteList();
        fuzzyDomainNameWhiteList.clear();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(systemRuleLocation))) {
            String line;
            boolean whiteListStart = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(WHITE_LIST_START_FLAG)) {
                    whiteListStart = true;
                    continue;
                }
                if (whiteListStart) {
                    if (PRECISE_MATCHER_PATTERN.matcher(line).find()) {
                        preciseDomainNameWhiteList.add(line.substring(DOMAIN_NAME_PRECISE_MATCHER.length()));
                    } else if (FUZZY_MATCHER_PATTERN.matcher(line).find()) {
                        fuzzyDomainNameWhiteList.add(line.substring(DOMAIN_NAME_FUZZY_MATCHER.length()));
                    }
                }
            }
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

}
