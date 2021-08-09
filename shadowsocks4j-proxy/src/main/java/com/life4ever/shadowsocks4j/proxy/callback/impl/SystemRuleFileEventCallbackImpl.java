package com.life4ever.shadowsocks4j.proxy.callback.impl;

import com.life4ever.shadowsocks4j.proxy.callback.FileEventCallback;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_FLAG;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_FUZZY_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_FLAG;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.DOMAIN_NAME_PRECISE_PATTERN;
import static com.life4ever.shadowsocks4j.proxy.consts.AdBlockPlusFilterConst.WHITE_LIST_START_FLAG;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SYSTEM_RULE_TXT;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.SYSTEM_RULE_TXT_LOCATION;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.clearSystemRuleWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getSystemRuleFuzzyDomainNameWhiteList;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getSystemRulePreciseDomainNameWhiteList;

public class SystemRuleFileEventCallbackImpl implements FileEventCallback {

    @Override
    public String getFileName() {
        return SYSTEM_RULE_TXT;
    }

    @Override
    public void resolveCreateEvent() throws Shadowsocks4jProxyException {
        resolveModifyEvent();
    }

    @Override
    public void resolveDeleteEvent() {
        clearSystemRuleWhiteList();
    }

    @Override
    public void resolveModifyEvent() throws Shadowsocks4jProxyException {
        clearSystemRuleWhiteList();
        List<String> preciseDomainNameWhiteList = getSystemRulePreciseDomainNameWhiteList();
        List<String> fuzzyDomainNameWhiteList = getSystemRuleFuzzyDomainNameWhiteList();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(SYSTEM_RULE_TXT_LOCATION))) {
            String line;
            boolean whiteListStart = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(WHITE_LIST_START_FLAG)) {
                    whiteListStart = true;
                    continue;
                }
                if (whiteListStart) {
                    if (DOMAIN_NAME_PRECISE_PATTERN.matcher(line).find()) {
                        preciseDomainNameWhiteList.add(line.substring(DOMAIN_NAME_PRECISE_FLAG.length()));
                    } else if (DOMAIN_NAME_FUZZY_PATTERN.matcher(line).find()) {
                        fuzzyDomainNameWhiteList.add(line.substring(DOMAIN_NAME_FUZZY_FLAG.length()));
                    }
                }
            }
        } catch (IOException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

}
