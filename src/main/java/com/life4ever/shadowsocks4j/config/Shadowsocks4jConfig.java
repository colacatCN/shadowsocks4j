package com.life4ever.shadowsocks4j.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Shadowsocks4jConfig {

    @JsonProperty("local")
    private LocalConfig localConfig;

    @JsonProperty("server")
    private ServerConfig serverConfig;

    @JsonProperty("cipher")
    private CipherConfig cipherConfig;

}
