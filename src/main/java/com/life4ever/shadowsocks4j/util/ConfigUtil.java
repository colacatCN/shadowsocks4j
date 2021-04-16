package com.life4ever.shadowsocks4j.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.life4ever.shadowsocks4j.consts.ConfigConst.APPLICATION_NAME;

/**
 * @author leo
 */
@Slf4j
public class ConfigUtil {

    private static final Properties PROPS = new Properties();

    private static void loadProperties() {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(APPLICATION_NAME)) {
            PROPS.load(inputStream);
            log.info(PROPS.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String getConfigValue(String configKey) {
        if (PROPS.isEmpty()) {
            loadProperties();
        }
        return PROPS.getProperty(configKey);
    }

}
