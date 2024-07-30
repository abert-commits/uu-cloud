package org.uu.wallet.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Simon
 * @date 2024/05/06
 */
public class UniqueCodeGeneratorUtil {

    /**
     * 生成6位邀请码
     */
    public static String generateInvitationCode() {
        StringBuilder sb = new StringBuilder();
        //产生length位的强随机数
        Random rd = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            // 产生0-2的3位随机数
            int type = rd.nextInt(3);
            switch (type) {
                case 0:
                    // 0-9的随机数
                    sb.append(rd.nextInt(10));
                    break;
                case 1:
                    // ASCII在65-90之间为大写,获取大写随机
                    sb.append((char) (rd.nextInt(25) + 65));
                    break;
                case 2:
                    // ASCII在97-122之间为小写，获取小写随机
                    sb.append((char) (rd.nextInt(25) + 97));
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }
}
