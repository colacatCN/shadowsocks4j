package com.life4ever.shadowsocks4j.proxy.service;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

/**
 * @author zhouke
 * @date 2021/06/16
 */
public interface IShadowsocks4jService {

    void start() throws Shadowsocks4jProxyException;

    void shutdownGracefully();

}
