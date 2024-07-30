package org.uu.wallet.tron.utils;

public class ConversionUtil {
    private static final String baseDigits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = baseDigits.length();
    //通过余数获取对应的64进制表示
    private static final char[] digitsChar = baseDigits.toCharArray();
    //这里预留了足够的空位122位
    private static final int FAST_SIZE = 'z';
    //这个是为了存放字母对应的值，比如-对应63，但是-是45，也就是 digitsIndex[45]=63
    //[digitsChar[-]会自动转变成45，这样子十六进制转十进制，就可以获取到前面的数字了。
    private static final int[] digitsIndex = new int[FAST_SIZE + 1];
    static {
        for (int i = 0; i < FAST_SIZE; i++) {
            digitsIndex[i] = -1;
        }
        //构造：a[117]=30这样的数组
        for (int i = 0; i < BASE; i++) {
            digitsIndex[digitsChar[i]] = i;
        }
    }
    //64进制转十进制
    public static long decode(String s) {
        long result = 0L;
        long multiplier = 1;
        for (int pos = s.length() - 1; pos >= 0; pos--) {
            int index = getIndex(s, pos);
            result += index * multiplier;
            multiplier *= BASE;
        }
        return result;
    }
    //十进制转64进制
    public static String encode(long number) {
        if (number < 0)
            System.out.println("Number(Base64) must be positive: " + number);
        if (number == 0)
            return "0";
        StringBuilder buf = new StringBuilder();
        while (number != 0) {
            //获取余数
            buf.append(digitsChar[(int) (number % BASE)]);
            //剩下的值
            number /= BASE;
        }
        //反转
        return buf.reverse().toString();
    }
    //获取对应的的64进制的值
    private static int getIndex(String s, int pos) {
        char c = s.charAt(pos);
        if (c > FAST_SIZE) {
            System.out.println("Unknow character for Base64: " + s);
        }
        int index = digitsIndex[c];
        if (index == -1) {
            System.out.println("Unknow character for Base64: " + s);
        }
        return index;
    }
}
