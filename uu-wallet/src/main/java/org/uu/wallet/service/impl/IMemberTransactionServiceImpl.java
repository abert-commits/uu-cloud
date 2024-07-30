package org.uu.wallet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.Enum.RewardTaskTypeEnum;
import org.uu.wallet.entity.TaskManager;
import org.uu.wallet.service.IControlSwitchService;
import org.uu.wallet.service.IMemberTaskStatusService;
import org.uu.wallet.service.IMemberTransactionService;
import org.uu.wallet.service.ITaskManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 统计会员每日交易数据
 *
 * @author Simon
 * @date 2024/03/22
 */
@Slf4j
@Service
public class IMemberTransactionServiceImpl implements IMemberTransactionService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IControlSwitchService controlSwitchService;

    @Autowired
    private ITaskManagerService taskManagerService;

    @Autowired
    private IMemberTaskStatusService memberTaskStatusService;


    /**
     * 更新会员每日交易信息
     *
     * @param buyMemberId
     * @param buyAmount
     * @param sellMemberId
     * @param sellAmount
     */
//    @Override
//    public void updateMemberDailyTransactionInfo(String buyMemberId, BigDecimal buyAmount, String sellMemberId, BigDecimal sellAmount) {
//
//        //累加会员每日买入金额
//        incrementBuyAmount(buyMemberId, buyAmount);
//
//        //累加会员每日买入次数
//        incrementBuyCount(buyMemberId);
//
//        //累加会员每日卖出金额
//        incrementSellAmount(sellMemberId, sellAmount);
//
//        //累加会员每日卖出次数
//        incrementSellCount(sellMemberId);
//
//
//        //判断是否开启了总任务开关
//        if (controlSwitchService.isTaskActive()) {
//
//            //获取任务信息
//            TaskManager taskDetailsByType = taskManagerService.getTaskDetailsByType(Integer.valueOf(RewardTaskTypeEnum.BUY.getCode()));
//            //判断是否开启了买入任务
//            if (taskDetailsByType != null) {
//                //处理买入会员的今日首次买入任务
//                boolean handleDailyTaskBuy = memberTaskStatusService.handleDailyBuyTask(Long.valueOf(buyMemberId), taskDetailsByType);
//
//                if (!handleDailyTaskBuy) {
//                    log.error("买入任务处理失败, 买入会员id: {}, 卖出会员id: {}", buyMemberId, sellMemberId);
//                }
//            } else {
//                log.info("交易成功处理, 买入任务活动未开启, 买入会员id: {}, 卖出会员id: {}", buyMemberId, sellMemberId);
//            }
//
//
//            //判断是否开启了卖出任务
//            if (taskManagerService.isSellTaskEnabled()) {
//                //处理卖出会员的今日首次卖出任务
//                boolean handleDailyTaskSell = memberTaskStatusService.handleDailySellTask(Long.valueOf(sellMemberId), taskDetailsByType);
//
//                if (!handleDailyTaskSell) {
//                    //每日任务处理失败, 手动抛出异常进行回滚
//                    throw new RuntimeException();
//                }
//            } else {
//                log.info("交易成功处理, 卖出任务活动未开启, 买入会员id: {}, 卖出会员id: {}", buyMemberId, sellMemberId);
//            }
//        } else {
//            log.info("交易成功处理, 总任务活动开关未开启, 买入会员id: {}, 卖出会员id: {}", buyMemberId, sellMemberId);
//        }
//    }


    /**
     * 构建Redis键的通用方法。
     *
     * @param type     交易类型（buyAmount, buyCount, sellAmount, sellCount）
     * @param memberId 会员ID
     * @return 构建好的键名
     */
    private String buildKey(String type, String memberId) {
        // 使用当前日期生成键名，确保数据按天分隔
        return String.format("member:%d:daily:%s:%s", memberId, type, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }


    /**
     * 设置Redis键的过期时间到次日凌晨。
     *
     * @param key Redis键名
     */
    private void setExpireAtMidnight(String key) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long secondsUntilMidnight = now.until(tomorrowMidnight, ChronoUnit.SECONDS);

        // 使用retryTemplate尝试设置过期时间
        retryTemplate(() -> redisTemplate.expire(key, Duration.ofSeconds(secondsUntilMidnight)), 3, 100);
    }


    /**
     * 累加会员每日买入金额
     *
     * @param memberId
     * @param amount
     */
    public void incrementBuyAmount(String memberId, BigDecimal amount) {
        String key = buildKey("buyAmount", memberId);
        retryTemplate(() -> redisTemplate.opsForValue().increment(key, amount.doubleValue()), 3, 100);
        setExpireAtMidnight(key);
    }

    /**
     * 累加会员每日买入次数。
     *
     * @param memberId 会员ID
     */
    public void incrementBuyCount(String memberId) {
        String key = buildKey("buyCount", memberId);
        retryTemplate(() -> redisTemplate.opsForValue().increment(key, 1), 3, 100); // 直接增加1次
        setExpireAtMidnight(key); // 确保键在次日凌晨过期
    }

    /**
     * 累加会员卖出金额
     *
     * @param memberId
     * @param amount
     */
    public void incrementSellAmount(String memberId, BigDecimal amount) {
        String key = buildKey("sellAmount", memberId);
        retryTemplate(() -> redisTemplate.opsForValue().increment(key, amount.doubleValue()), 3, 100);
        setExpireAtMidnight(key);
    }

    /**
     * 累加会员每日卖出次数。
     *
     * @param memberId 会员ID
     */
    public void incrementSellCount(String memberId) {
        String key = buildKey("sellCount", memberId);
        retryTemplate(() -> redisTemplate.opsForValue().increment(key, 1), 3, 100); // 直接增加1次
        setExpireAtMidnight(key); // 确保键在次日凌晨过期
    }


    /**
     * 获取今日买入金额
     *
     * @param memberId
     * @return {@link Double}
     */
    @Override
    public Double getBuyAmount(String memberId) {
        String key = buildKey("buyAmount", memberId);
        return getValue(key);
    }

    /**
     * 获取今日买入次数
     *
     * @param memberId
     * @return {@link Double}
     */
    @Override
    public Double getBuyCount(String memberId) {
        String key = buildKey("buyCount", memberId);
        return getValue(key);
    }

    /**
     * 获取今日卖出金额
     *
     * @param memberId
     * @return {@link Double}
     */
    @Override
    public Double getSellAmount(String memberId) {
        String key = buildKey("sellAmount", memberId);
        return getValue(key);
    }

    /**
     * 获取今日卖出次数
     *
     * @param memberId
     * @return {@link Double}
     */
    @Override
    public Double getSellCount(String memberId) {
        String key = buildKey("sellCount", memberId);
        return getValue(key);
    }

    private Double getValue(String key) {
        final Double[] result = {0.0};
        boolean success = retryTemplate(() -> {
            Object value = redisTemplate.opsForValue().get(key);
            result[0] = value != null ? Double.parseDouble(value.toString()) : 0.0;
        }, 3, 100);
        return success ? result[0] : null;
    }


    /**
     * 重试模板，用于在执行Redis操作时添加重试机制。
     *
     * @param redisOperation 要执行的操作
     * @param maxRetries     最大重试次数
     * @param delay          重试间隔时间（毫秒）
     * @return 操作是否成功
     */
    public boolean retryTemplate(Runnable redisOperation, int maxRetries, long delay) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                redisOperation.run();
                return true; // 操作成功，直接返回
            } catch (Exception e) {
                System.out.println("Attempt " + (attempt + 1) + " failed. Reason: " + e.getMessage());
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(delay); // 等待后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false; // 中断时退出
                    }
                    delay *= 2; // 增加等待时间，实现指数退避
                }
            }
        }
        return false; // 如果重试次数耗尽仍失败，则返回false
    }

}
