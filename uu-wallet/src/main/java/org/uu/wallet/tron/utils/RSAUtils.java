package org.uu.wallet.tron.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtils {


    private static Map<Integer,String> keyMap=new HashMap<>();
    public static void main( String[] args ) throws Exception {
        //生成公钥和私钥
        getKeyPair();
        //加密字符串
        String password="s7142eb581fc05d66ee8ci90559898a281e7912c02m0d09a5c87c3fbae4aebo74e29m";
        System.out.println("随机生成的公钥为："+keyMap.get(0));
        System.out.println("随机生成的私钥为："+keyMap.get(1));
        String passwordEn=enCodeKey(password,keyMap.get(0));
        System.out.println(password+"\t加密后的字符串为："+passwordEn);
        String passwordDe=deCodeKey(passwordEn,keyMap.get(1));
        System.out.println("还原后的字符串为："+passwordDe);
    }
    /**
     * 随机生成密钥对
     * @throws NoSuchAlgorithmException
     */
    public static void getKeyPair() throws Exception {
        //KeyPairGenerator类用于生成公钥和密钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        //初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024,new SecureRandom());
        //生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();//得到私钥
        PublicKey publicKey = keyPair.getPublic();//得到公钥
        //得到公钥字符串
        String publicKeyString=new String(Base64.encodeBase64(publicKey.getEncoded()));
        //得到私钥字符串
        String privateKeyString=new String(Base64.encodeBase64(privateKey.getEncoded()));
        //将公钥和私钥保存到Map
        keyMap.put(0,publicKeyString);//0表示公钥
        keyMap.put(1,privateKeyString);//1表示私钥
    }
    /**
     * RSA公钥加密
     *
     * @param str
     *            加密字符串
     * @param publicKey
     *            公钥
     * @return 密文
     * @throws Exception
     *             加密过程中的异常信息
     */
    public static String encrypt(String str,String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey= (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RAS加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,pubKey);
        String outStr=Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str
     *            加密字符串
     * @param privateKey
     *            私钥
     * @return 铭文
     * @throws Exception
     *             解密过程中的异常信息
     */
    public static String decrypt(String str,String privateKey) throws Exception {
        //Base64解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        //Base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        PrivateKey priKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,priKey);
        String outStr=new String(cipher.doFinal(inputByte));
        return outStr;

    }


    public static String enCodeKey(String str,String publicKey) throws Exception {
        StringBuilder keySb = new StringBuilder(str);
        String newKey =  keySb.reverse().toString();
        StringBuilder newSb = new StringBuilder("");
        char[] chars = newKey.toCharArray();
        for(int i = 0; i < chars.length; i++){
            newSb.append(ConversionUtil.encode(62-ConversionUtil.decode(String.valueOf(chars[i]))));
        }
        return encrypt(newSb.toString(),publicKey);
    }

    public static String deCodeKey(String str,String privateKey) throws Exception {
        String deCode = decrypt(str,privateKey);
        String newKey =  deCode.replaceAll("10","+");
        StringBuilder newSb = new StringBuilder("");
        char[] chars = newKey.toCharArray();
        for(int i = 0; i < chars.length; i++){
            if(String.valueOf(chars[i]).equals("+") ){
                newSb.append(0);
            }else{
                newSb.append(ConversionUtil.encode(62-ConversionUtil.decode(String.valueOf(chars[i]))));
            }
        }
        return newSb.reverse().toString().toLowerCase();
    }

}
