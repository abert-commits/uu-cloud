package org.uu.wallet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.service.IPaymentOrderService;
import org.uu.wallet.service.UpiTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UpiTransactionServiceImpl implements UpiTransactionService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IPaymentOrderService paymentOrderService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");


    /**
     * 增加单日交易笔数并且标记为已处理
     *
     * @param memberId
     * @param orderId
     */
    @Override
    public void incrementDailyTransactionCountAndMarkAsProcessed(String memberId, String orderId) {
        log.info("增加会员当日收款次数并标记为已处理, memberId: {}, 订单号: {}", memberId, orderId);

        String orderSetKey = generateOrderSetKey(memberId, "increment"); // 生成一个基于memberId的唯一键，用来存储已处理的订单ID
        Boolean orderAlreadyProcessed = redisTemplate.opsForSet().isMember(orderSetKey, orderId);

        // 判断该笔订单是否已经处理过
        if (!Boolean.TRUE.equals(orderAlreadyProcessed)) {
            String transactionCountKey = generateTransactionCountKey(memberId);

            // 重试逻辑封装的方法中执行以下操作
            retryTemplate(() -> {
                // 增加交易笔数
                redisTemplate.opsForValue().increment(transactionCountKey, 1);
                // 将该笔订单标记为已处理
                redisTemplate.opsForSet().add(orderSetKey, orderId);

                // 设置键的过期时间到当日凌晨00:00，确保每日重置
                setExpirationAtMidnight(transactionCountKey);
                setExpirationAtMidnight(orderSetKey);

                log.info("会员当日收款次数+1并标记处理成功, memberId: {}, 订单号: {}", memberId, orderId);
            }, 3, 100); // 重试3次，初始延迟100毫秒
        }
    }


    /**
     * 减少当日收款次数
     *
     * @param memberId
     * @param orderId
     */
    @Override
    public void decrementDailyTransactionCountIfApplicable(String memberId, String orderId) {

        // 判断是否拆单 如果拆单的话 查看有没有进行中或成功的子订单
        if (orderId.startsWith("C2C")) {
            // 对于C2C开头的母订单，检查是否有活跃或成功的子订单
            if (hasActiveOrSuccessfulSubOrders(orderId)) {
                // 如果有成功的子订单或未结束的子订单，则不进行后续操作
                log.info("upi当日收款次数-1, 该笔订单是母订单, 并且状态还在进行中或有未完成的子订单, 不进行操作, memberId: {}, 订单号: {}", memberId, orderId);
                return;
            }
        }

        // 如果不是C2C开头的订单，或C2C订单没有活跃/成功的子订单，则进行减少操作
        decrementDailyTransactionCountIfExistAndMarkAsProcessed(memberId, orderId);
    }


    // 减少单日交易笔数（如果存在）并且标记为已处理
    public void decrementDailyTransactionCountIfExistAndMarkAsProcessed(String memberId, String orderId) {
        String orderSetKey = generateOrderSetKey(memberId, "decrement"); // 生成一个基于memberId的唯一键，用来存储已处理的订单ID
        Boolean orderAlreadyProcessed = redisTemplate.opsForSet().isMember(orderSetKey, orderId);

        //判断该笔订单是否已经减少过收款次数
        if (!Boolean.TRUE.equals(orderAlreadyProcessed)) {
            //该笔订单没被处理过 将交易笔数-1
            String transactionCountKey = generateTransactionCountKey(memberId);

            retryTemplate(() -> {

                // 在减少之前，首先检查交易次数是否大于0
                Object value = redisTemplate.opsForValue().get(transactionCountKey);
                Long currentCount = 0L; // 默认值为0
                //校验是不是数字
                if (value instanceof Number) {
                    currentCount = ((Number) value).longValue();
                }

                // 仅当当前计数大于0时，才减少交易次数
                if (currentCount > 0) {
                    Boolean exists = redisTemplate.hasKey(transactionCountKey);
                    if (Boolean.TRUE.equals(exists)) {
                        redisTemplate.opsForValue().increment(transactionCountKey, -1);

                        //将该笔订单标记为已处理
                        redisTemplate.opsForSet().add(orderSetKey, orderId);
                        //设置过期时间到当日凌晨00:00
                        setExpirationAtMidnight(orderSetKey);

                        log.info("会员当日收款次数-1处理成功, memberId: {}, 订单号: {}", memberId, orderId);
                    }
                }
            }, 3, 100); // 重试3次，初始延迟100毫秒
        }
    }

    // 生成基于memberId的订单集合键
    public String generateOrderSetKey(String memberId, String operationType) {
        // 获取当前日期，格式为: YYYYMMDD
        String currentDate = DATE_FORMAT.format(LocalDate.now());
        // 生成并返回基于memberId、操作类型和当前日期的唯一键
        return "memberId:" + operationType + ":" + memberId + ":ordersProcessed:" + currentDate;
    }

    // 获取单日交易笔数
    @Override
    public Long getDailyTransactionCount(String memberId) {
        String key = generateTransactionCountKey(memberId);

        // Try to get the value for the given key
        Object value = redisTemplate.opsForValue().get(key);

        // Check if the value is not null and an instance of Number
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            // If the value is null or not an instance of Number, return 0L as the default value
            return 0L;
        }
    }

    // 生成交易笔数的键
    @Override
    public String generateTransactionCountKey(String memberId) {
        return "collection:count:" + memberId + ":date:" + DATE_FORMAT.format(LocalDate.now());
    }

    // 设置键的过期时间为当天午夜
    @Override
    public void setExpirationAtMidnight(String key) {
        long ttl = calculateSecondsUntilMidnight();
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    // 计算当前时间至午夜的秒数
    @Override
    public long calculateSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        return java.time.Duration.between(now, midnight).getSeconds();
    }


    /**
     * 操作redis 添加重试机制
     *
     * @param redisOperation
     * @param maxRetries     最多重试次数
     * @param delay          重试之间 间隔时间
     * @return boolean
     */
    private boolean retryTemplate(Runnable redisOperation, int maxRetries, long delay) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                redisOperation.run();
                return true; // 操作成功，返回
            } catch (Exception e) {
                System.out.println("Redis operation failed, attempt: " + (attempt + 1));
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(delay); // 等待一段时间后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // 恢复中断状态
                        return false; // 中断时退出
                    }
                    delay *= 2; // 增加等待时间，实现简单的指数退避
                } else {
                    return false; // 达到最大重试次数，仍然失败
                }
            }
        }
        return false;
    }


    /**
     * 假设的方法，用于检查是否存在活跃或成功的子订单
     *
     * @param motherOrderId
     * @return boolean
     */
    public boolean hasActiveOrSuccessfulSubOrders(String motherOrderId) {
        // 返回true如果存在活跃或成功的子订单，否则返回false
        return paymentOrderService.existsActiveSubOrders(motherOrderId);
    }
}
