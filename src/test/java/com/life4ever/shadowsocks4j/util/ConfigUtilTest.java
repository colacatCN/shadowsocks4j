package com.life4ever.shadowsocks4j.util;

import com.life4ever.shadowsocks4j.config.CipherConfig;
import com.life4ever.shadowsocks4j.config.LocalConfig;
import com.life4ever.shadowsocks4j.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ConfigUtilTest {

    @Test
    public void testGetLocalConfig() {
        LocalConfig localConfig = ConfigUtil.getLocalConfig();
        log.info("localConfig = {}", localConfig);
    }

    @Test
    public void testGetServerConfig() {
        ServerConfig serverConfig = ConfigUtil.getServerConfig();
        log.info("serverConfig = {}", serverConfig);
    }

    @Test
    public void testGetCipherConfig() {
        CipherConfig cipherConfig = ConfigUtil.getCipherConfig();
        log.info("cipherConfig = {}", cipherConfig);
    }
}