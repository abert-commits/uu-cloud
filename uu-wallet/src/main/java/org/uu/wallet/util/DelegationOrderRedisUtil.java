package org.uu.wallet.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.bo.DelegationOrderBO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DelegationOrderRedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonUtil redissonUtil;

    private static final String ORDER_KEY = "delegationOrders";

    /**
     * 将订单添加到 Redis.
     *
     * @param order 订单对象
     */
    public void addOrder(DelegationOrderBO order) {
        // 先删除已有的订单记录（如果存在）
        removeOrderByMemberId(order.getMemberId());

        // 添加新的订单记录
        double score = calculateScore(order.getAmount(), order.getDelegationTime());
        redisTemplate.opsForZSet().add(ORDER_KEY, serializeOrder(order), score);
    }

    /**
     * 根据会员ID删除订单.
     *
     * @param memberId 会员ID
     */
    private void removeOrderByMemberId(String memberId) {
        Set<Object> orders = redisTemplate.opsForZSet().range(ORDER_KEY, 0, -1);
        if (orders != null && !orders.isEmpty()) {
            for (Object orderString : orders) {
                DelegationOrderBO order = deserializeOrder((String) orderString);
                if (order.getMemberId().equals(memberId)) {
                    redisTemplate.opsForZSet().remove(ORDER_KEY, orderString);
                    break;
                }
            }
        }
    }

    /**
     * 计算分数，用于在 Redis 中排序。
     *
     * @param amount         委托金额
     * @param delegationTime 委托时间
     * @return 分数
     */
    private double calculateScore(BigDecimal amount, LocalDateTime delegationTime) {
        long timeAsScore = delegationTime.toEpochSecond(ZoneOffset.UTC);
        return amount.doubleValue() + (double) timeAsScore / 1e12;
    }

    /**
     * 序列化订单对象。
     *
     * @param order 订单对象
     * @return 序列化后的字符串
     */
    private String serializeOrder(DelegationOrderBO order) {
        return JSON.toJSONString(order);
    }

    /**
     * 反序列化订单对象。
     *
     * @param orderString 序列化后的字符串
     * @return 订单对象
     */
    private DelegationOrderBO deserializeOrder(String orderString) {
        return JSON.parseObject(orderString, DelegationOrderBO.class);
    }

    /**
     * 从 Redis 中匹配一个订单.
     *
     * @param amount 指定的金额，用于匹配订单
     * @return 匹配到的订单对象，如果没有匹配到，则返回 null
     */
    public DelegationOrderBO matchOrder(BigDecimal amount) {


        //分布式锁key ar-wallet-matchOrder
        String key = "uu-wallet-matchOrder";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                Set<Object> orders = redisTemplate.opsForZSet().rangeByScore(ORDER_KEY, amount.doubleValue(), Double.MAX_VALUE);


                if (orders != null && !orders.isEmpty()) {
                    for (Object orderString : orders) {
                        DelegationOrderBO order = deserializeOrder((String) orderString);

                        // 如果订单的剩余金额大于等于请求的金额
                        if (order.getAmount().compareTo(amount) >= 0) {
                            return order;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("委托订单匹配失败, e: {}", e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 如果没有匹配到订单，返回null
        return null;
    }

    /**
     * 从 Redis 中移除订单。
     *
     * @param order 要移除的订单对象
     */
    public void removeOrder(DelegationOrderBO order) {
        Set<Object> orders = redisTemplate.opsForZSet().range(ORDER_KEY, 0, -1);
        if (orders != null && !orders.isEmpty()) {
            for (Object orderString : orders) {
                DelegationOrderBO existingOrder = deserializeOrder((String) orderString);
                if (existingOrder.getMemberId().equals(order.getMemberId())) {
                    redisTemplate.opsForZSet().remove(ORDER_KEY, orderString);
                    break;
                }
            }
        }
    }
}
