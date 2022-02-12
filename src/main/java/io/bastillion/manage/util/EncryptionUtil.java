/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility to encrypt, decrypt, and hash
 */
public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    //secret key
    private static byte[] key = new byte[0];

    static {
        try {
            key = KeyStoreUtil.getSecretBytes(KeyStoreUtil.ENCRYPTION_KEY_ALIAS);
        } catch (GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }

    public static final String CRYPT_ALGORITHM = "AES";
    public static final String HASH_ALGORITHM = "SHA-256";

    private EncryptionUtil() {
    }

    /**
     * generate salt for hash
     *
     * @return salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return new String(Base64.encodeBase64(salt));
    }

    /**
     * return hash value of string
     *
     * @param str  unhashed string
     * @param salt salt for hash
     * @return hash value of string
     */
    public static String hash(String str, String salt) throws NoSuchAlgorithmException {
        String hash = null;
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        if (StringUtils.isNotEmpty(salt)) {
            md.update(Base64.decodeBase64(salt.getBytes()));
        }
        md.update(str.getBytes(StandardCharsets.UTF_8));
        hash = new String(Base64.encodeBase64(md.digest()));

        return hash;
    }

    /**
     * return hash value of string
     *
     * @param str unhashed string
     * @return hash value of string
     */
    public static String hash(String str) throws NoSuchAlgorithmException {
        return hash(str, null);
    }

    /**
     * return encrypted value of string
     *
     * @param key secret key
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(byte[] key, String str) throws GeneralSecurityException {

        String retVal = null;
        if (str != null && str.length() > 0) {
            Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
            byte[] encVal = c.doFinal(str.getBytes());
            retVal = new String(Base64.encodeBase64(encVal));


        }
        return retVal;
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param key secret key
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(byte[] key, String str) throws GeneralSecurityException {
        String retVal = null;
        if (str != null && str.length() > 0) {
            Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
            byte[] decodedVal = Base64.decodeBase64(str.getBytes());
            retVal = new String(c.doFinal(decodedVal));
        }
        return retVal;
    }

    /**
     * return encrypted value of string
     *
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(String str) throws GeneralSecurityException {
        return encrypt(key, str);
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(String str) throws GeneralSecurityException {
        return decrypt(key, str);
    }


    /**
     * return encrypted value of string with static key
     *
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encryptStatic(String str) throws GeneralSecurityException {
        return encrypt(staticKey, str);
    }

    /**
     * return decrypted value of encrypted string with static key
     *
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decryptStatic(String str) throws GeneralSecurityException {
        return decrypt(staticKey, str);
    }

    private static final byte[] staticKey = { 12, 49, -63, 87, 42, -77, 126, -64, -32, -58, -126, 54, 35, 1, -113, 82,
            -10, 12, -90, 12, 17, 127, -91, 93, -43, 46, -7, -100, -96, 12, 108, -101 };

}
