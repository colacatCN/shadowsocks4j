package com.life4ever.shadowsocks4j.proxy.cipher.impl;

import com.life4ever.shadowsocks4j.proxy.cipher.AbstractCipherFunction;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

import static com.life4ever.shadowsocks4j.proxy.constant.CipherConfigConstant.CHACHA20;

public class Chacha20CipherFunctionImpl extends AbstractCipherFunction {

    public Chacha20CipherFunctionImpl(String password, String salt, int secretKeyLength, String mode) throws Shadowsocks4jProxyException {
        super(CHACHA20, password, salt, secretKeyLength, mode);
    }

    @Override
    protected AlgorithmParameterSpec createParameterSpecForEncryption(byte[] nonce) {
        return new IvParameterSpec(nonce);
    }

    @Override
    protected AlgorithmParameterSpec createParameterSpecForDecryption(byte[] encryptedContent) {
        return new IvParameterSpec(encryptedContent, 0, NONCE_LENGTH);
    }

}
