package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PacConfig {

    @JsonProperty("enable-pac-mode")
    private Boolean enablePacMode;

    @JsonProperty("system-rule")
    private RuleConfig systemRuleConfig;

    @JsonProperty("user-rule")
    private RuleConfig userRuleConfig;

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

    public RuleConfig getSystemRuleConfig() {
        return systemRuleConfig;
    }

    public void setSystemRuleConfig(RuleConfig systemRuleConfig) {
        this.systemRuleConfig = systemRuleConfig;
    }

    public RuleConfig getUserRuleConfig() {
        return userRuleConfig;
    }

    public void setUserRuleConfig(RuleConfig userRuleConfig) {
        this.userRuleConfig = userRuleConfig;
    }

}
