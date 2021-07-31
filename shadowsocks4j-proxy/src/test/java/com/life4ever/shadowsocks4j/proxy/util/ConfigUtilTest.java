package com.life4ever.shadowsocks4j.proxy.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.DEFAULT_CIPHER_METHOD;
import static com.life4ever.shadowsocks4j.proxy.consts.Shadowsocks4jProxyConst.LOOPBACK_ADDRESS;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getCipherMethod;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getCipherPassword;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getLocalServerInetSocketAddress;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.getRemoteServerInetSocketAddress;

public class ConfigUtilTest {

    @Test
    public void test() {
        InetSocketAddress localServerInetSocketAddress = getLocalServerInetSocketAddress();
        Assert.assertEquals(LOOPBACK_ADDRESS, localServerInetSocketAddress.getHostString());
        Assert.assertEquals(10721, localServerInetSocketAddress.getPort());

        InetSocketAddress remoteServerInetSocketAddress = getRemoteServerInetSocketAddress();
        Assert.assertEquals(LOOPBACK_ADDRESS, remoteServerInetSocketAddress.getHostString());
        Assert.assertEquals(10727, remoteServerInetSocketAddress.getPort());

        Assert.assertEquals("123456", getCipherPassword());
        Assert.assertEquals(DEFAULT_CIPHER_METHOD, getCipherMethod());
    }

}
