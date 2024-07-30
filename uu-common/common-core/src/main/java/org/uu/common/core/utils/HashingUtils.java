package org.uu.common.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtils {

    /**
     * 生成字符串的MD5哈希值。
     *
     * @param data 需要生成哈希值的字符串
     * @return MD5哈希值的十六进制表示
     * @throws NoSuchAlgorithmException 如果当前环境不支持MD5算法
     */
    public static String md5Hash(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
