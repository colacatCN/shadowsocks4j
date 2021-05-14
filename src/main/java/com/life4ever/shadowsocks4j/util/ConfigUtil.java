package com.life4ever.shadowsocks4j.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.life4ever.shadowsocks4j.config.CipherConfig;
import com.life4ever.shadowsocks4j.config.LocalConfig;
import com.life4ever.shadowsocks4j.config.ServerConfig;
import com.life4ever.shadowsocks4j.config.Shadowsocks4jConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author leo
 */
@Slf4j
public class ConfigUtil {

    private static final String APPLICATION_YML = "application.yml";

    private static final ObjectMapper MAPPER = new YAMLMapper().findAndRegisterModules();

    private static Shadowsocks4jConfig shadowsocks4jConfig;

    private ConfigUtil() {
    }

    private static void instantiateShadowsocks4jConfig() {
        try (InputStream yamlInputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(APPLICATION_YML)) {
            shadowsocks4jConfig = MAPPER.readValue(yamlInputStream, Shadowsocks4jConfig.class);
            log.info(shadowsocks4jConfig.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static LocalConfig getLocalConfig() {
        if (shadowsocks4jConfig == null) {
            instantiateShadowsocks4jConfig();
        }
        return shadowsocks4jConfig.getLocalConfig();
    }

    public static ServerConfig getServerConfig() {
        if (shadowsocks4jConfig == null) {
            instantiateShadowsocks4jConfig();
        }
        return shadowsocks4jConfig.getServerConfig();
    }

    public static CipherConfig getCipherConfig() {
        if (shadowsocks4jConfig == null) {
            instantiateShadowsocks4jConfig();
        }
        return shadowsocks4jConfig.getCipherConfig();
    }

}
