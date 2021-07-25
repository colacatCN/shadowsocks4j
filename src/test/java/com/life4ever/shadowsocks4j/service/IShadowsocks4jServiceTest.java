package com.life4ever.shadowsocks4j.service;

import com.life4ever.shadowsocks4j.Shadowsocks4jApplicationTest;
import com.life4ever.shadowsocks4j.exception.Shadowsocks4jException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IShadowsocks4jServiceTest extends Shadowsocks4jApplicationTest {

    @Autowired
    private IShadowsocks4jService shadowsocks4jService;

    @Test
    public void test() throws Shadowsocks4jException {
        shadowsocks4jService.start();
    }

}
