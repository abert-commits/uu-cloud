package org.uu.wallet.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 正则表达式校验相关工具类
 * */
public class RegexUtil {


    /**
     * 校验登录密码格式是否正确
     *
     * @param password
     * @return boolean
     */
    public static boolean validatePassword(String password) {
        // 正则表达式匹配包含数字和字母且长度在8到32之间的密码
        String regex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,32}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    /**
     * 校验印度手机号码格式是否正确
     *
     * @param phoneNumber
     * @return boolean
     */
    public static boolean validatePhoneNumber(String phoneNumber) {
        // 手机号码正则表达式
        String regex = "^\\d{8,13}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }


    /**
     * 校验邮箱账号格式是否正确
     *
     * @param email
     * @return boolean
     */
    public static boolean validateEmail(String email) {
        // 正则表达式验证邮箱格式
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    /**
     * 校验印度身份证号格式是否正确
     *
     * @param idCardNumber
     * @return boolean
     */
    public static boolean validateAadharNumber(String idCardNumber) {
        // 印度身份证号正则表达式
        String regex = "^[0-9]{4}[0-9]{8}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(idCardNumber);
        return matcher.matches();
    }

    /**
     * 校验卖出平台订单号格式是否正确
     *
     * @param sellPlatformOrder
     * @return boolean
     */
    public static boolean validateSellPlatformOrder(String sellPlatformOrder) {
        // 印度身份证号正则表达式
        String regex = "^MC\\d{17}\\d{5}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sellPlatformOrder);
        return matcher.matches();
    }

    /**
     * 校验买入平台订单号格式是否正确
     *
     * @param buyPlatformOrder
     * @return boolean
     */
    public static boolean validateBuyPlatformOrder(String buyPlatformOrder) {
        // 印度身份证号正则表达式
        String regex = "^MR\\d{17}\\d{5}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buyPlatformOrder);
        return matcher.matches();
    }

    /**
     * 校验匹配池平台订单号格式是否正确
     *
     * @param buyPlatformOrder
     * @return boolean
     */
    public static boolean validateMatchPoolPlatformOrder(String buyPlatformOrder) {
        // 印度身份证号正则表达式
        String regex = "^C2C\\d{17}\\d{5}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buyPlatformOrder);
        return matcher.matches();
    }
}
