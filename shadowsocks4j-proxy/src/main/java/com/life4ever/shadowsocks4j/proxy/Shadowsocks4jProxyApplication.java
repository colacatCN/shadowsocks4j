package com.life4ever.shadowsocks4j.proxy;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;
import com.life4ever.shadowsocks4j.proxy.service.IShadowsocks4jService;
import com.life4ever.shadowsocks4j.proxy.service.impl.Shadowsocks4jLocalServiceImpl;
import com.life4ever.shadowsocks4j.proxy.service.impl.Shadowsocks4jRemoteServiceImpl;

import static com.life4ever.shadowsocks4j.proxy.enums.ProxyModeEnum.LOCAL;
import static com.life4ever.shadowsocks4j.proxy.enums.ProxyModeEnum.REMOTE;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateLocalMode;
import static com.life4ever.shadowsocks4j.proxy.util.ConfigUtil.activateRemoteMode;

public class Shadowsocks4jProxyApplication {

    public static void main(String[] args) throws Shadowsocks4jProxyException {
        if (args.length == 0) {
            throw new Shadowsocks4jProxyException("请配置启动参数");
        }

        String proxyMode = args[0];
        IShadowsocks4jService shadowsocks4jService;
        if (LOCAL.name().equalsIgnoreCase(proxyMode)) {
            activateLocalMode();
            shadowsocks4jService = new Shadowsocks4jLocalServiceImpl();
        } else if (REMOTE.name().equalsIgnoreCase(proxyMode)) {
            activateRemoteMode();
            shadowsocks4jService = new Shadowsocks4jRemoteServiceImpl();
        } else {
            throw new Shadowsocks4jProxyException("启动参数错误");
        }
        shadowsocks4jService.start();
    }

}
