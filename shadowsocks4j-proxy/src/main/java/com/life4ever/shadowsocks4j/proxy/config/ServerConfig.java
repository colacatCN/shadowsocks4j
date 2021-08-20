package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerConfig {

    private String ip;

    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

}
