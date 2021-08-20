package com.life4ever.shadowsocks4j.proxy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CipherConfig {

    private String password;

    private String salt;

    private String method;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "CipherConfig{" +
                "password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

}
