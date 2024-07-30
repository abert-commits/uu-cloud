package org.uu.wallet.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.uu.wallet.entity.EncryptedData;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Comparator;

public class RsaUtil {

    // 使用公钥加密数据，并返回 Base64 编码的字符串
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 使用私钥解密数据
    public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        // Base64 解码
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);

        // 使用私钥进行 RSA 解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        // 将解密后的数据转换为字符串
        return new String(decryptedBytes);
    }

    // 使用私钥签名
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        // 将签名转换为Base64编码的字符串
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // 使用公钥验证签名
    public static boolean verify(String data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(data.getBytes(StandardCharsets.UTF_8)); // 明确指定编码
        return sign.verify(signature);
    }

    // 生成RSA密钥对
    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        return keyPair;
    }

    // 获取字符串格式的公钥
    public static String getPublicKeyAsString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // 获取字符串格式的私钥
    public static String getPrivateKeyAsString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // 将字符串转换为公钥
    public static PublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    // 将字符串转换为私钥
    public static PrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(byteKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }


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

    // 使用公钥加密AES密钥
    public static String encryptAESKey(SecretKey aesKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
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


    /**
     * 签名并加密数据
     *
     * @param data       要签名并加密的数据
     * @param privateKey 用于签名的私钥
     * @param publicKey  用于加密AES密钥的公钥
     * @return 加密后的数据和AES密钥的封装对象
     * @throws Exception
     */
    public static EncryptedData signAndEncryptData(Object data, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        // 转换为JSON对象并移除可能存在的sign字段
        JSONObject jsonData = new JSONObject(JSON.parseObject(JSON.toJSONString(data)));
        jsonData.remove("sign");

        // 对JSON数据进行排序
        String sortedJsonData = JsonUtil.sortJsonByKey(jsonData);

        // 使用私钥进行签名
        String signature = RsaUtil.sign(sortedJsonData, privateKey);

        // 将签名加回到JSON数据中
        jsonData.put("sign", signature);

        // 加密数据
        return encryptData(jsonData, publicKey);
    }

    /**
     * 获取签名
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String generateSign(Object data, PrivateKey privateKey) throws Exception {
        // 转换为JSON对象并移除可能存在的sign字段
        JSONObject jsonData = new JSONObject(JSON.parseObject(JSON.toJSONString(data)));
        jsonData.remove("sign");

        // 对JSON数据进行排序
        String sortedJsonData = JsonUtil.sortJsonByKey(jsonData);

        // 使用私钥进行签名
        return RsaUtil.sign(sortedJsonData, privateKey);
    }

    /**
     * 验证签名
     *
     * @param data      要验证签名的数据
     * @param signature 签名
     * @param publicKey 用于验证签名的公钥
     * @return 验签是否成功
     * @throws Exception
     */
    public static Boolean verifySignature(Object data, String signature, PublicKey publicKey) throws Exception {
        // 转换为JSON对象并移除可能存在的sign字段
        JSONObject jsonData = new JSONObject(JSON.parseObject(JSON.toJSONString(data)));
        jsonData.remove("sign");

        // 检查是否存在bankInfoList，并按bankCardNumber值排序
//        if (jsonData.containsKey("bankInfoList")) {
//            JSONArray bankInfoList = jsonData.getJSONArray("bankInfoList");
//            bankInfoList.sort(Comparator.comparing(obj -> ((JSONObject) obj).getString("bankCardNumber")));
//
//            // 对bankInfoList里的每个JSONObject按键名排序
//            for (int i = 0; i < bankInfoList.size(); i++) {
//                JSONObject bankInfo = bankInfoList.getJSONObject(i);
//                String sortedBankInfo = JSON.toJSONString(bankInfo, SerializerFeature.MapSortField);
//                bankInfoList.set(i, JSON.parseObject(sortedBankInfo));
//            }
//
//            jsonData.put("bankInfoList", bankInfoList);
//        }

        // 对整个JSON数据进行排序
        String sortedJsonData = JSON.toJSONString(jsonData, SerializerFeature.MapSortField);

        // 对JSON数据进行排序
//        String sortedJsonData = JsonUtil.sortJsonByKey(jsonData);

        // 使用公钥验证签名
        return RsaUtil.verify(sortedJsonData, Base64.getDecoder().decode(signature), publicKey);
    }

    /**
     * 加密数据
     *
     * @param data
     * @param merchantPublicKey
     * @return {@link EncryptedData}
     * @throws Exception
     */
    private static EncryptedData encryptData(Object data, PublicKey merchantPublicKey) throws Exception {

        // 将Java对象转换为JSON字符串
        String jsonData = JSON.toJSONString(data);

        // 1. 动态生成一个AES密钥
        SecretKey aesKey = RsaUtil.generateAESKey();

        // 2. 使用AES密钥加密数据
        String encryptedData = RsaUtil.encryptData(jsonData, aesKey);

        // 3. 使用RSA公钥加密AES密钥
        String encryptedAESKey = RsaUtil.encryptAESKey(aesKey, merchantPublicKey);

        // 封装加密数据和加密后的AES密钥
        return new EncryptedData(encryptedData, encryptedAESKey);
    }

    /**
     * 解密数据
     *
     * @param encryptedKey  加密后的AES密钥
     * @param encryptedData 加密后的数据
     * @param privateKey    用于解密AES密钥的私钥
     * @param clazz         要转换的Java对象类型
     * @return 解密后的Java对象
     * @throws Exception
     */
    public static <T> T decryptData(String encryptedKey, String encryptedData, PrivateKey privateKey, Class<T> clazz) throws Exception {
        // 1. 使用RSA私钥解密AES密钥
        SecretKey secretKey = RsaUtil.decryptAESKey(encryptedKey, privateKey);

        // 2. 使用解密后的AES密钥解密数据
        String decryptedData = RsaUtil.decryptData(encryptedData, secretKey);

        // 3. 将解密后的JSON数据转换为Java对象
        return JSON.parseObject(decryptedData, clazz);
    }

}
