package com.life4ever.shadowsocks4j.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhouke
 * @date 2021/06/16
 */
@Data
public class ShadowsocksConfig {

    @JsonProperty("local-server")
    private ServerConfig localServerConfig;

    @JsonProperty("remote-server")
    private ServerConfig remoteServerConfig;

    @JsonProperty("cipher")
    private CipherConfig cipherConfig;

}
