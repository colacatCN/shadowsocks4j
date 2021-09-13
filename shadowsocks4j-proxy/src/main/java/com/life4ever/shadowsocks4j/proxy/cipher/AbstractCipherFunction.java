package com.life4ever.shadowsocks4j.proxy.cipher;

import com.life4ever.shadowsocks4j.proxy.enums.CipherAlgorithmEnum;
import com.life4ever.shadowsocks4j.proxy.exception.Shadowsocks4jProxyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import static com.life4ever.shadowsocks4j.proxy.constant.CipherAlgorithmConstant.SECRET_KEY_ALGORITHM;
import static com.life4ever.shadowsocks4j.proxy.constant.CipherAlgorithmConstant.SECRET_KEY_ITERATION_COUNT;

public abstract class AbstractCipherFunction implements ICipherFunction {

    protected static final int NONCE_LENGTH = 12;

    protected static final int EXTRA_LENGTH = 4;

    protected static final int HEADER_LENGTH = NONCE_LENGTH + EXTRA_LENGTH;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CipherAlgorithmEnum cipherAlgorithm;

    private final SecretKeySpec secretKeySpec;

    protected AbstractCipherFunction(String password,
                                     String salt,
                                     int secretKeyLength,
                                     CipherAlgorithmEnum cipherAlgorithm)
            throws Shadowsocks4jProxyException {
        this.cipherAlgorithm = cipherAlgorithm;
        this.secretKeySpec = createSecretKeySpec(password, salt, secretKeyLength);
    }

    @Override
    public byte[] encrypt(byte[] content) throws Shadowsocks4jProxyException {
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm.getMode());

            // 生成 nonce
            byte[] nonce = new byte[NONCE_LENGTH];
            SECURE_RANDOM.nextBytes(nonce);

            // 生成 extra
            byte[] extra = createExtra();

            // 生成加密的 parameterSpec
            AlgorithmParameterSpec parameterSpec = createParameterSpecForEncryption(nonce);

            // 执行加密
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);
            byte[] encryptedContent = cipher.doFinal(content);
            return Base64.getMimeEncoder().encode(mergeAllBytes(nonce, extra, encryptedContent));
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] decrypt(byte[] content) throws Shadowsocks4jProxyException {
        try {
            byte[] encryptedContent = Base64.getMimeDecoder().decode(content);
            Cipher cipher = Cipher.getInstance(cipherAlgorithm.getMode());

            // 生成解密的 parameterSpec
            AlgorithmParameterSpec parameterSpec = createParameterSpecForDecryption(encryptedContent);

            // 执行解密
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);
            return cipher.doFinal(encryptedContent, HEADER_LENGTH, encryptedContent.length - HEADER_LENGTH);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    protected abstract AlgorithmParameterSpec createParameterSpecForEncryption(byte[] nonce);

    protected abstract AlgorithmParameterSpec createParameterSpecForDecryption(byte[] encryptedContent);

    protected byte[] createExtra() {
        return new byte[EXTRA_LENGTH];
    }

    private SecretKeySpec createSecretKeySpec(String password, String salt, int length) throws Shadowsocks4jProxyException {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), SECRET_KEY_ITERATION_COUNT, length);
            return new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), cipherAlgorithm.getName());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Shadowsocks4jProxyException(e.getMessage(), e);
        }
    }

    private byte[] mergeAllBytes(byte[]... bytesArray) {
        if (bytesArray == null) {
            return new byte[]{};
        }

        int length = 0;
        for (byte[] bytes : bytesArray) {
            if (bytes == null) {
                continue;
            }
            length += bytes.length;
        }

        byte[] result = new byte[length];
        int currentIndex = 0;
        for (byte[] bytes : bytesArray) {
            if (bytes == null) {
                continue;
            }
            System.arraycopy(bytes, 0, result, currentIndex, bytes.length);
            currentIndex += bytes.length;
        }

        return result;
    }

}
