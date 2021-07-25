package com.life4ever.shadowsocks4j.service;

import com.life4ever.shadowsocks4j.exception.Shadowsocks4jException;

/**
 * @author zhouke
 * @date 2021/06/16
 */
public interface IShadowsocks4jService {

    void start() throws Shadowsocks4jException;

    void shutdownGracefully();

}
