package org.uu.wallet.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@Slf4j
public class AESUtils {

    private static final String EncryptAlg ="AES";

    private static final String Cipher_Mode="AES/ECB/PKCS7Padding";

    private static final String Encode="UTF-8";

    /**
     * AES加密 - CBC/PKCS5Padding
     */
    @SneakyThrows
    public static String encrypt(String content, String strKey) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            byte[] raw = strKey.getBytes(Encode);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, EncryptAlg);
            Cipher cipher = Cipher.getInstance(Cipher_Mode);//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            byte[] data=cipher.doFinal(content.getBytes(Encode));
            String result= Base64.encodeBase64String(data);
            return result;
        } catch (Exception e) {
            throw new Exception("AES加密失败："+e.getMessage(),e);
        }
    }

    /**
     * AES解密 - CBC/PKCS5Padding
     */
    @SneakyThrows
    public static String decrypt(String content, String strKey) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            log.warn("加密内容content:{},password:{}",content,strKey);
            log.warn("编码Encode:{}",Encode);
            byte[] raw = strKey.getBytes(Encode);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, EncryptAlg);
            log.warn("加密模式:{}",Cipher_Mode);
            Cipher cipher = Cipher.getInstance(Cipher_Mode);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = new Base64().decode(content);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,Encode);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception e) {
            throw new Exception("AES解密失败：" +e.getMessage(),e);
        }
    }

    public static String encryptFroKyc(String text, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), EncryptAlg);
        Cipher cipher = Cipher.getInstance(EncryptAlg);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(text.getBytes());
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptForKyc(String encryptedText, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), EncryptAlg);
        Cipher cipher = Cipher.getInstance(EncryptAlg);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedText));
        return new String(decryptedBytes);
    }

}