package com.life4ever.shadowsocks4j.proxy.config;

public class RuleConfig {

    private String location;

    private String updateUrl;

    private String updateInterval;

    public RuleConfig() {
    }

    public RuleConfig(String location, String updateUrl, String updateInterval) {
        this.location = location;
        this.updateUrl = updateUrl;
        this.updateInterval = updateInterval;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(String updateInterval) {
        this.updateInterval = updateInterval;
    }

}
