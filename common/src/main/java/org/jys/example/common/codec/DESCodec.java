package org.jys.example.common.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * @author YueSong Jiang
 * @date 2019/1/12
 * DES CBC encrypt and decrypt tools
 */
public class DESCodec {

    private static final String KEY_ALGORITHM = "DES";
    private static final String CIPHER_ALGORITHM = "DES/CBC/NoPadding";

    private static final Logger logger = LoggerFactory.getLogger(DESCodec.class);

    /**
     * generate des key and encode to string use base64 in order to save
     *
     * @return string des key, the key may be not use readable
     */
    public static String generateKey() {
        KeyGenerator keyGenerator;
        SecretKey key = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(56);
            key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            logger.error("no algorithm with name {}", KEY_ALGORITHM);
            logger.error(e.getMessage(), e);
        }
        return key == null ? null : Base64Utils.encodeToString(key.getEncoded());
    }

    /**
     * get random and user readable key
     * also can used to generate initial vector
     *
     * @param size key size
     * @return utf-8 encoded string
     */
    public static String generateKey(int size) {
        Random random = new Random();
        byte[] secret = new byte[size];
        for (int i = 0; i < secret.length; i++) {
            secret[i] = (byte) (random.nextInt(94) + 32);
        }
        return new String(secret, Charset.forName("UTF-8"));
    }

    /**
     * convert from encoded base64 key to des key
     *
     * @param keyArray secret key array
     * @return security key
     * @throws InvalidKeyException giving keyArray is wrong
     */
    private static Key bytesToKey(byte[] keyArray) throws InvalidKeyException {
        DESKeySpec desKeySpec = new DESKeySpec(keyArray);
        Key key = null;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            key = keyFactory.generateSecret(desKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("unexpected exception when get key", e);
        }
        return key;
    }

    /**
     * encrypt data use des cbc mode
     *
     * @param data     raw data
     * @param keyArray secret key
     * @param iv       initial vector
     * @return encrypted data
     */
    public static byte[] encrypt(byte[] data, byte[] keyArray, byte[] iv) {
        byte[] encryptedArray = null;
        try {
            Key key = bytesToKey(keyArray);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            encryptedArray = cipher.doFinal(data);
        } catch (Exception e) {
            logger.error("error happened when encrypt data {}", data);
            logger.error(e.getMessage(), e);
        }
        return encryptedArray;
    }

    /**
     * decrypt data use des cbc mode
     *
     * @param data     encoded data
     * @param keyArray secret key
     * @param iv       initial vector
     * @return decrypted data
     */
    public static byte[] decrypt(byte[] data, byte[] keyArray, byte[] iv) {
        byte[] decryptedArray = null;
        try {
            Key key = bytesToKey(keyArray);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            decryptedArray = cipher.doFinal(data);
        } catch (Exception e) {
            logger.error("error happened when decrypt data {}", data);
            logger.error(e.getMessage(), e);
        }
        return decryptedArray;
    }
}
