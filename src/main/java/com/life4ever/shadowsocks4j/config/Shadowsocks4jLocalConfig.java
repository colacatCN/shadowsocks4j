package com.life4ever.shadowsocks4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouke
 * @date 2021/06/16
 */
@ConfigurationProperties(prefix = "shadowsocks4j.local")
@Configuration
@Data
public class Shadowsocks4jLocalConfig {

    private int port;

}
