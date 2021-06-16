package com.life4ever.shadowsocks4j.config;

import com.life4ever.shadowsocks4j.Shadowsocks4jApplicationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhouke
 * @date 2021/06/16
 */
@Slf4j
public class Shadowsocks4jConfigTest extends Shadowsocks4jApplicationTest {

    @Autowired
    private Shadowsocks4jLocalConfig localConfig;

    @Test
    public void test() {
        log.info("localConfig = {}", localConfig);
    }

}
