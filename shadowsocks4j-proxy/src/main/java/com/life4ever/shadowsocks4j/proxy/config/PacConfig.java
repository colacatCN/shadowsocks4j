package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PacConfig {

    @JsonProperty("enable-pac-mode")
    private Boolean enablePacMode;

    @JsonProperty("update-url")
    private String updateUrl;

    @JsonProperty("update-interval")
    private Long updateInterval;

    public PacConfig() {
    }

    public PacConfig(Boolean enablePacMode) {
        this.enablePacMode = enablePacMode;
    }

    public Boolean isEnablePacMode() {
        return enablePacMode;
    }

    public void setEnablePacMode(Boolean enablePacMode) {
        this.enablePacMode = enablePacMode;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(Long updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public String toString() {
        return "PacConfig{" +
                "enablePacMode=" + enablePacMode +
                ", updateUrl='" + updateUrl + '\'' +
                ", updateInterval=" + updateInterval +
                '}';
    }

}
