package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shadowsocks4jProxyConfig {

    @JsonProperty("local-server")
    private ServerConfig localServerConfig;

    @JsonProperty("remote-server")
    private ServerConfig remoteServerConfig;

    @JsonProperty("cipher")
    private CipherConfig cipherConfig;

    @JsonProperty("pac")
    private PacConfig pacConfig;

    public ServerConfig getLocalServerConfig() {
        return localServerConfig;
    }

    public void setLocalServerConfig(ServerConfig localServerConfig) {
        this.localServerConfig = localServerConfig;
    }

    public ServerConfig getRemoteServerConfig() {
        return remoteServerConfig;
    }

    public void setRemoteServerConfig(ServerConfig remoteServerConfig) {
        this.remoteServerConfig = remoteServerConfig;
    }

    public CipherConfig getCipherConfig() {
        return cipherConfig;
    }

    public void setCipherConfig(CipherConfig cipherConfig) {
        this.cipherConfig = cipherConfig;
    }

    public PacConfig getPacConfig() {
        return pacConfig;
    }

    public void setPacConfig(PacConfig pacConfig) {
        this.pacConfig = pacConfig;
    }

    @Override
    public String toString() {
        return "Shadowsocks4jProxyConfig{" +
                "localServerConfig=" + localServerConfig +
                ", remoteServerConfig=" + remoteServerConfig +
                ", cipherConfig=" + cipherConfig +
                ", pacConfig=" + pacConfig +
                '}';
    }

}
