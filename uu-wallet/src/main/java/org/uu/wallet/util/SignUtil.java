package org.uu.wallet.util;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;


public class SignUtil {

    /**
     * 生成随机32位md5Key
     *
     * @return {@link String}
     */
    public static String generateMd5Key() {
        UUID uuid = UUID.randomUUID();
        return getMD5(uuid.toString().replace("-", ""), 1, uuid.toString());
    }

    /*
     * 组装支付下单接口签名参数
     * */
    public static String sortObject(Object vo) {

        //将VO对象转为TreeMap
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(vo, Map.class);
        TreeMap<String, Object> sortedMap = new TreeMap<>(map);

        //去掉sign字段
        sortedMap.remove("sign");

        return sortData(sortedMap, "&");
    }

    public static String sortData(Map<String, Object> sortedMap, String link) {
        if (StringUtils.isEmpty(link)) {
            link = "&";
        }

        Set<Entry<String, Object>> entrySet = sortedMap.entrySet();
        StringBuffer sbf = new StringBuffer();
        for (Entry<String, Object> entry : entrySet) {
            if (null != entry.getValue() && StringUtils.isNotEmpty(entry.getValue().toString())) {
                sbf.append(entry.getKey()).append("=").append(entry.getValue()).append(link);
            }
        }
        String returnStr = sbf.toString();
        if (returnStr.endsWith(link)) {
            returnStr = returnStr.substring(0, returnStr.length() - 1);
        }
        return returnStr;
    }

    public static String getMD5(String code, Integer isVerified, String redismd5key) {
        try {

            //待加密字符串
            String str = code + isVerified + redismd5key;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(str.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

