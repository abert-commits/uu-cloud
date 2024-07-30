package org.uu.auth.security.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Base64;

public class RsaUtil {

    // 生成AES密钥
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 也可以选择192或256位
        return keyGen.generateKey();
    }

    // 使用AES密钥加密数据
    public static String encryptData(String data, Key aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }


    public static SecretKey decryptAESKey(String encryptedAESKey, PrivateKey privateKey) throws Exception {
        // 将加密的AES密钥从Base64格式转换回字节数组
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedAESKey);

        // 初始化RSA Cipher为解密模式，使用私钥
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // 解密AES密钥
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedKeyBytes);

        // 将解密后的字节转换为SecretKey
        return new SecretKeySpec(decryptedKeyBytes, "AES");
    }

    // 使用AES密钥解密数据
    public static String decryptData(String encryptedData, SecretKey aesKey) throws Exception {
        // 将加密数据从Base64格式转换回字节数组
        byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);

        // 初始化AES Cipher为解密模式，使用AES密钥
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        // 解密数据
        byte[] decryptedDataBytes = cipher.doFinal(encryptedDataBytes);

        // 将解密后的字节转换为字符串
        return new String(decryptedDataBytes, StandardCharsets.UTF_8);
    }

}
