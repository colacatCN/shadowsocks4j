package com.life4ever.shadowsocks4j.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shadowsocks4jDTO {

    private InetSocketAddress inetSocketAddress;

    private byte[] payload;

    public String getHostname() {
        return getInetSocketAddress().getHostName();
    }

    public int getPort() {
        return getInetSocketAddress().getPort();
    }

}
