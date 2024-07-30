package org.uu.common.redis.util;

public class BuildDistributedLockFlagUtil {
    /**
     * 构建分布式锁标识
     * @param appFlag APP标识
     * @param prefix 前缀
     * @param uniqueFlag 唯一标识
     */
    public static String buildDistributedLockFlag(String appFlag, String prefix, String uniqueFlag) {
        return String.format("%s-%s-%s", appFlag, prefix, uniqueFlag);
    }

    /**
     * 构建分布式锁标识(APP标识为"69pay")
     * @param prefix 前缀
     * @param uniqueFlag 唯一标识
     */
    public static String buildDistributedLockFlag(String prefix, String uniqueFlag) {
        return buildDistributedLockFlag("69pay", prefix, uniqueFlag);
    }
}
