package com.life4ever.shadowsocks4j.proxy.service;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

public interface IShadowsocks4jService {

    void start() throws Shadowsocks4jProxyException;

    void shutdownGracefully();

}
