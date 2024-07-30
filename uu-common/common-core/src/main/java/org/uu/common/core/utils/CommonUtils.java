package org.uu.common.core.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.redis.util.RedisUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Admin
 */
@Slf4j
public class CommonUtils {

    private final static int MINUTE = 5;


    /**
     * 订单号生成规则: 前缀(账变枚举) + 年月日时分秒毫秒 + 5位随机数
     * 示例: MR2022041215151588888
     * @param prefix
     * @return
     */
    public static String generateOrderNo(String prefix){

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String suffix = fmt.format(LocalDateTime.now(ZoneId.systemDefault()));
        return prefix + suffix + RandomUtil.randomNumbers(5);

    }

    /**
     * 计算下一次起始时间
     * @param time
     * @param suffix
     * @return
     */
    public static String nextTime(String time, String suffix){
        String date = time + suffix;
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime(date, GlobalConstants.DATE_FORMAT_MINUTE);
        return DateUtil.format(localDateTime.plusMinutes(MINUTE),GlobalConstants.DATE_FORMAT_MINUTE);
    }


    public static String getMerchantCode(String merchantStr){

        if(StringUtils.isEmpty(merchantStr)){
            return "";
        }else {
            return merchantStr.split(":")[0];
        }
    }


    public static String getMerchantName(String merchantStr){

        if(StringUtils.isEmpty(merchantStr)){
            return "";
        }else {
            return merchantStr.split(":")[1];
        }
    }


    /**
     * 当一个用户登录时，就往map中构建一个k-v键值对
     * k- 用户名，v 当前时间+过期时间间隔，这里以60s为例子
     * 如果用户在过期时间间隔内频繁对网站进行操作，那么对应
     * 她的登录凭证token的有效期也会一直续期，因此这里使用用户名作为k可以覆盖之前
     * 用户登录的旧值，从而不会出现重复统计的情况
     */
    public static void insertToken(String uid, RedisUtils redisUtils, Long timeOut){
        long currentTime = System.currentTimeMillis();
        redisUtils.hset(GlobalConstants.ONLINE_USER_KEY, uid, currentTime + timeOut * 60 * 1000);
    }


    /**
     * 当用户注销登录时，将移除map中对应的键值对
     * 避免当用户下线时，该计数器还错误的将该用户当作
     * 在线用户进行统计
     * @param uid
     */
    public static void deleteToken(String uid, RedisUtils redisUtils){
        redisUtils.hdel(GlobalConstants.ONLINE_USER_KEY, uid);
    }

    /**
     * 统计用户在线的人数
     * @return
     */
    public static Long getOnlineCount(RedisUtils redisUtils){
        Long onlineCount = 0L;
        Map<Object, Object> userList = redisUtils.hashKey(GlobalConstants.ONLINE_USER_KEY);
        long currentTime = System.currentTimeMillis();
        for (Object name : userList.keySet()) {
            Long value = (Long) userList.get(name);
            if (value > currentTime){
                // 说明该用户登录的令牌还没有过期
                onlineCount++;
            }else {
                redisUtils.hdel(GlobalConstants.ONLINE_USER_KEY, name);
            }
        }
        return onlineCount;
    }

    public static int calculateMonthDiff(LocalDateTime date1, LocalDateTime date2){
        int year1 = date1.getYear();
        int month1 = date1.getMonthValue();

        int year2 = date2.getYear();
        int month2 = date2.getMonthValue();

        int totalMonthDiff = (year2 - year1) * 12;
        int monthDiff = month2 - month1 + totalMonthDiff;

        return monthDiff;
    }


    /**
     * 添加在线蚂蚁
     * @param cacheKey 缓存KEY
     * @param antName 蚂蚁名称
     * @param redisUtils RedisUtil
     * @param expireTime 过期时间(单位:秒)
     */
    public static void addOnlineAnt(String cacheKey, String antName, RedisUtils redisUtils, Long expireTime) {
        redisUtils.set(String.format("%s:%s", cacheKey, antName), System.currentTimeMillis(), expireTime);
    }

    /**
     * 删除在线蚂蚁
     * @param cacheKey 缓存KEY
     * @param antName 蚂蚁名称
     * @param redisUtils RedisUtil
     */
    public static void removeOnlineAnt(String cacheKey, String antName, RedisUtils redisUtils) {
        redisUtils.del(String.format("%s:%s", cacheKey, antName));
    }
}

