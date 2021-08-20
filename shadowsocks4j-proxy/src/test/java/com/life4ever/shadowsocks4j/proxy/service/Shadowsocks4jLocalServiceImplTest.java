package com.life4ever.shadowsocks4j.proxy.service;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.service.impl.Shadowsocks4jLocalServiceImpl;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateLocalMode;

@Ignore
public class Shadowsocks4jLocalServiceImplTest {

    private IShadowsocks4jService shadowsocks4jService;

    @Test
    public void test() throws Shadowsocks4jProxyException {
        activateLocalMode();
        shadowsocks4jService = new Shadowsocks4jLocalServiceImpl();
        shadowsocks4jService.start();
    }

    @After
    public void after() {
        shadowsocks4jService.shutdownGracefully();
    }

}
