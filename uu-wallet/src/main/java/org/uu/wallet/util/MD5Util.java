package org.uu.wallet.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 生成输入字符串的MD5哈希值
     *
     * @param input 输入字符串
     * @return MD5哈希值
     */
    public static String generateMD5(String input) {
        try {
            // 获取MD5算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 更新摘要
            md.update(input.getBytes());
            // 计算哈希值
            byte[] digest = md.digest();
            // 将字节数组转换为十六进制字符串
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法找到MD5算法", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 将每个字节转换为两位十六进制数
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}