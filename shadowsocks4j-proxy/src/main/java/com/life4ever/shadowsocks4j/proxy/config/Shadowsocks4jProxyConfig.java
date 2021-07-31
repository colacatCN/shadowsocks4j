package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Shadowsocks4jProxyConfig {

    @JsonProperty("local-server")
    private ServerConfig localServerConfig;

    @JsonProperty("remote-server")
    private ServerConfig remoteServerConfig;

    @JsonProperty("cipher")
    private CipherConfig cipherConfig;

}
