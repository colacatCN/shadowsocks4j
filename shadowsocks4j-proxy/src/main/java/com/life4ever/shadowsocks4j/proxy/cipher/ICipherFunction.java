package com.life4ever.shadowsocks4j.proxy.cipher;

import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

public interface ICipherFunction {

    byte[] encrypt(byte[] content) throws Shadowsocks4jProxyException;

    byte[] decrypt(byte[] content) throws Shadowsocks4jProxyException;

}
