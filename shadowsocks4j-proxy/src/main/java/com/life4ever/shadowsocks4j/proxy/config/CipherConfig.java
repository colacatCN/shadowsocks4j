package com.life4ever.shadowsocks4j.proxy.config;

public class CipherConfig {

    private String password;

    private String method;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
                ", method='" + method + '\'' +
                '}';
    }

}
