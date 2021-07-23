package com.life4ever.shadowsocks4j.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class CipherConfig {

    private String password;

    private String method;

}
