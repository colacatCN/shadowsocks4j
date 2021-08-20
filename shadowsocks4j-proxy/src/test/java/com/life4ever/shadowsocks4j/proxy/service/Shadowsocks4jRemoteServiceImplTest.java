package com.life4ever.shadowsocks4j.proxy.service;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.service.impl.Shadowsocks4jRemoteServiceImpl;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateRemoteMode;

@Ignore
public class Shadowsocks4jRemoteServiceImplTest {

    private IShadowsocks4jService shadowsocks4jService;

    @Test
    public void test() throws Shadowsocks4jProxyException {
        activateRemoteMode();
        shadowsocks4jService = new Shadowsocks4jRemoteServiceImpl();
        shadowsocks4jService.start();
    }

    @After
    public void after() {
        shadowsocks4jService.shutdownGracefully();
    }

}
