package org.uu.wallet.util;

import java.util.Random;

public class SmsCodeGeneratorUtil {

    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);

//        return "123456";
    }
}
