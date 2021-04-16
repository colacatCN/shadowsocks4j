package com.life4ever.shadowsocks4j.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class ConfigUtilTest {

    @Test
    public void getConfigValue() {
        String clientName = ConfigUtil.getConfigValue("client.name");
        log.info("clientName = {}", clientName);
    }
}