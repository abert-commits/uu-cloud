package org.uu.wallet.util;

import java.util.Random;

/**
 * 生成随机昵称工具类
 *
 * @author Simon
 * @date 2023/12/09
 */
public class NicknameGeneratorUtil {

    private static final int NICKNAME_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    public static String generateNickname() {
        StringBuilder nickname = new StringBuilder(NICKNAME_LENGTH);
        for (int i = 0; i < NICKNAME_LENGTH; i++) {
            nickname.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return nickname.toString();
    }
}