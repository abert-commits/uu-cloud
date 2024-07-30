package org.uu.wallet.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.wallet.Enum.MemberTypeEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.entity.TradIpBlackMessage;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.entity.TradeIpBlacklist;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.BuyListReq;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.service.ITradeIpBlacklistService;
import org.uu.wallet.service.impl.TradeConfigServiceImpl;
import org.uu.wallet.vo.BuyListVo;
import org.uu.wallet.vo.MemberInformationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.uu.common.redis.constants.RedisKeys.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {

    private final RedisTemplate redisTemplate;
    private final TradeConfigServiceImpl tradeConfigService;
    //从nacos获取配置
    private final ArProperty arProperty;

    @Autowired
    private ITradeIpBlacklistService tradeIpBlacklistService;

    @Autowired
    private RabbitMQService rabbitMQService;


    /**
     * 记录交易并检查是否达到限制
     *
     * @param ip              交易发生的IP地址
     * @param orderId         订单ID
     * @param transactionTime 交易时间
     * @return 是否达到交易限制
     */
    public void recordTransactionAndCheckLimit(String ip, String orderId, LocalDateTime transactionTime) {
        String key = RedisKeys.TRADE + ip;
        String hashKey = orderId;
        String value = String.valueOf(transactionTime.toEpochSecond(ZoneOffset.UTC));

        // 检查订单ID是否已存在 不存在才进行添加
        if (!redisTemplate.opsForHash().hasKey(key, hashKey)) {
            redisTemplate.opsForHash().put(key, hashKey, value);
            // 设置过期时间为24小时
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }

        // 获取当前时间减去配置的小时数(3小时)的Epoch秒数
        long thresholdEpochSecond = LocalDateTime.now().minusHours(arProperty.getExpirationHours()).toEpochSecond(ZoneOffset.UTC);

        // 使用StringRedisTemplate时，entries(key)的返回类型应该是Map<String, String>
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        List<String> recentOrderIds = entries.entrySet().stream()
                // 由于entries是Map<Object, Object>，这里需要进行显式类型转换
                .map(entry -> (Map.Entry<String, String>) (Map.Entry<?, ?>) entry)
                .filter(entry -> Long.parseLong(entry.getValue()) > thresholdEpochSecond)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 检查是否达到交易限制
        if (recentOrderIds != null && recentOrderIds.size() >= arProperty.getTradeLimit()) {
            //已到达限制, 添加该黑名单前, 先检查该ip是否处于黑名单内
            if (!tradeIpBlacklistService.isIpBlacklisted(ip)) {
                //该ip不存在黑名单列表中, 将该ip添加到黑名单中
                TradeIpBlacklist tradeIpBlacklist = new TradeIpBlacklist();

                tradeIpBlacklist.setIpAddress(ip);

                tradeIpBlacklist.setReason("在 " + arProperty.getExpirationHours() + " 小时内已达到 " + arProperty.getTradeLimit() + "次交易笔数, 系统自动添加到黑名单");

                tradeIpBlacklist.setCreateBy("系统");
                if (tradeIpBlacklistService.save(tradeIpBlacklist)) {
                    //添加黑名单成功
                    log.info("添加交易ip黑名单, 成功, 订单号: {}, 交易ip: {}", orderId, ip);
                    TradIpBlackMessage tradIpBlackMessage = new TradIpBlackMessage();
                    tradIpBlackMessage.setType("1");
                    tradIpBlackMessage.setAutoFlag("1");
                    tradIpBlackMessage.setTradeIpBlacklist(tradeIpBlacklist);
                    rabbitMQService.sendTradeIpBlackAddMessage(tradIpBlackMessage);
                } else {
                    //添加黑名单失败
                    log.error("添加交易ip黑名单, 失败, 订单号: {}, 交易ip: {}", orderId, ip);
                }
            } else {
                //该ip已达到黑名单限制, 但是黑名单已经存在该ip了, 所以不进行添加操作
                log.info("添加交易ip黑名单, 该ip已达到黑名单限制, 但是黑名单已经存在该ip了, 所以不进行添加操作, 订单号: {}, 交易ip: {}", orderId, ip);
            }
        }
    }

    /**
     * 记录会员30分钟内买入失败次数
     *
     * @param memberId
     */
    public void recordMemberBuyFailure(String memberId) {

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigService.getById(1);

        //连续失败次数
        Integer numberFailures = tradeConfig.getNumberFailures();

        //禁用买入时间
        Integer disabledTime = tradeConfig.getDisabledTime();

        //后台有配置这个限制 才进行操作
        if (numberFailures != null && numberFailures > 0 && disabledTime != null && disabledTime > 0) {

            String failureKey = RedisKeys.MEMBER_BUY_FAILURE + memberId;
            String blockKey = RedisKeys.MEMBER_BUY_BLOCKED + memberId;

            // 递增失败次数
            Long currentAttempts = redisTemplate.opsForValue().increment(failureKey);

            // 设置失败次数的过期时间（如果是第一次失败）
            if (currentAttempts == 1) {
                redisTemplate.expire(failureKey, 30, TimeUnit.DAYS);
            }

            // 检查是否达到失败次数
            if (currentAttempts >= numberFailures) {
                // 设置 禁用买入时间 (小时冷却期)
                redisTemplate.opsForValue().set(blockKey, disabledTime, disabledTime, TimeUnit.HOURS);

                // 删除失败次数
                redisTemplate.delete(failureKey);

                log.info("会员30分钟内买入失败次数达到阈值, 已被限制买入, 会员id: {}, 会员失败次数: {}, 后台失败次数限制阈值: {}, 禁用买入时间(小时): {}", memberId, currentAttempts, numberFailures, disabledTime);
            }

            log.info("记录会员30分钟内买入失败次数 会员id: {}, 会员失败次数: {}, 后台失败次数限制阈值: {}, 禁用买入时间(小时): {}", memberId, currentAttempts, numberFailures, disabledTime);
        } else {
            log.info("记录会员30分钟内买入失败次数: 后台未开启此限制 会员id: {}, 配置信息: {}, 连续失败次数: {}, 禁用买入时间(小时): {} ", memberId, tradeConfig, numberFailures, disabledTime);
        }
    }


    /**
     * 会员买入成功后清除失败次数记录和冷却期限制
     *
     * @param memberId 会员ID
     */
    public void clearMemberBuyFailureAndCooldown(String memberId) {
        String failureKey = RedisKeys.MEMBER_BUY_FAILURE + memberId;
        String blockKey = RedisKeys.MEMBER_BUY_BLOCKED + memberId;

        // 检查并删除买入失败次数记录
        Boolean hasFailures = redisTemplate.hasKey(failureKey);
        if (Boolean.TRUE.equals(hasFailures)) {
            redisTemplate.delete(failureKey);
            log.info("会员交易成功, 清除买入失败次数记录, 会员id: {}", memberId);
        } else {
            log.info("会员交易成功, 但未找到失败次数记录, 会员id: {}", memberId);
        }

        // 检查并删除冷却期限制
        Boolean isBlocked = redisTemplate.hasKey(blockKey);
        if (Boolean.TRUE.equals(isBlocked)) {
            redisTemplate.delete(blockKey);
            log.info("会员交易成功, 清除买入冷却期限制, 会员id: {}", memberId);
        } else {
            log.info("会员交易成功, 但未处于冷却期, 会员id: {}", memberId);
        }
    }


    /**
     * 检查是否处于买入冷却期
     *
     * @param memberId
     * @return boolean
     */
    public boolean canMemberBuy(String memberId) {
        String blockKey = RedisKeys.MEMBER_BUY_BLOCKED + memberId;

        // 检查是否处于冷却期
        return !redisTemplate.hasKey(blockKey);
    }

    /**
     * 获取会员被禁止买入的时间。
     *
     * @param memberId
     * @return 剩余过期时间（秒）。如果 key 不存在，返回 -2；如果 key 存在但没有设置过期时间，返回 -1。
     */
    public Integer getMemberBuyBlockRemainingTime(String memberId) {

        String blockKey = RedisKeys.MEMBER_BUY_BLOCKED + memberId;

        // 获取会员被禁用的时间
        Integer blockReason = (Integer) redisTemplate.opsForValue().get(blockKey);

        return blockReason;
    }


    /**
     * 获取会员买入冷却期的剩余时间。
     *
     * @param memberId
     * @return 剩余过期时间（秒）。如果 key 不存在，返回 -2；如果 key 存在但没有设置过期时间，返回 -1。
     */
    public long getMemberBuyBlockedExpireTime(String memberId) {
        String blockKey = RedisKeys.MEMBER_BUY_BLOCKED + memberId;
        return redisTemplate.getExpire(blockKey, TimeUnit.SECONDS);
    }


    /**
     * 设置匹配剩余时间
     *
     * @param orderNo
     * @param durationMinutes
     */
    public void setMatchExpireTime(String orderNo, long durationMinutes) {
        String key = RedisKeys.ORDER_MATCH_EXPIRE + orderNo;
        redisTemplate.opsForValue().set(key, "matchData", durationMinutes, TimeUnit.MINUTES);
    }

    /**
     * 获取匹配剩余时间
     *
     * @param orderNo
     * @return long
     */
    public long getMatchRemainingTime(String orderNo) {
        String key = RedisKeys.ORDER_MATCH_EXPIRE + orderNo;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 设置确认剩余时间
     *
     * @param orderNo
     * @param durationMinutes
     */
    public void setConfirmExpireTime(String orderNo, long durationMinutes) {
        String key = RedisKeys.ORDER_CONFIRM_EXPIRE + orderNo;
        redisTemplate.opsForValue().set(key, "confirmData", durationMinutes, TimeUnit.MINUTES);
    }


    /**
     * 获取确认剩余时间
     *
     * @param orderNo
     * @return long
     */
    public long getConfirmRemainingTime(String orderNo) {
        String key = RedisKeys.ORDER_CONFIRM_EXPIRE + orderNo;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    /**
     * 设置支付剩余时间
     *
     * @param orderNo
     * @param durationMinutes
     */
    public void setPaymentExpireTime(String orderNo, long durationMinutes) {
        String key = RedisKeys.ORDER_PAYMENT_EXPIRE + orderNo;
        redisTemplate.opsForValue().set(key, "paymentData", durationMinutes, TimeUnit.MINUTES);
    }

    /**
     * 获取支付剩余时间
     *
     * @param orderNo
     * @return long
     */
    public long getPaymentRemainingTime(String orderNo) {
        String key = RedisKeys.ORDER_PAYMENT_EXPIRE + orderNo;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    /**
     * 设置USDT支付剩余时间
     *
     * @param orderNo
     * @param durationMinutes
     */
    public void setUsdtPaymentExpireTime(String orderNo, long durationMinutes) {
        String key = RedisKeys.ORDER_USDT_PAYMENT_EXPIRE + orderNo;
        redisTemplate.opsForValue().set(key, "usdtPaymentData", durationMinutes, TimeUnit.MINUTES);
    }


    /**
     * 获取USDT支付剩余时间
     *
     * @param orderNo
     * @return long
     */
    public long getUsdtPaymentRemainingTime(String orderNo) {
        String key = RedisKeys.ORDER_USDT_PAYMENT_EXPIRE + orderNo;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 将买入金额列表订单添加到redis
     * type: 1 右边添加  type: 2 左边添加 (新的订单: 右边添加 放在最后,  被取消的订单: 左边添加, 放在最前)
     */
    public void addOrderIdToList(BuyListVo buyListVo, String type) {

        //使用 Redis 哈希表存储订单详情
        String orderDetailsKey = RedisKeys.ORDER_DETAILS + buyListVo.getPlatformOrder();

        //检查订单是否存在 不存在才进行添加
        if (!redisTemplate.opsForHash().hasKey(orderDetailsKey, buyListVo.getPlatformOrder())) {
            //使用 Redis 列表存储订单 ID (带重试机制)
            if (tryPush(RedisKeys.ORDERS_LIST, buyListVo.getPlatformOrder(), 3, 100, type)) {

                log.info("买入金额列表, 存储订单id到redis成功, redisKey:{}, 订单号: {}", orderDetailsKey, buyListVo.getPlatformOrder());

                // 准备订单详情数据
                Map<String, Object> orderDetailsMap = JSON.parseObject(JSON.toJSONString(buyListVo), Map.class);

                if (tryPutAllHash(orderDetailsKey, orderDetailsMap, 3, 100)) {
                    log.info("买入金额列表, 存储订单详情到Redis哈希表成功, key:{}, 订单详情: {}", orderDetailsKey, orderDetailsMap);
                    // 以金额为分值存入
                    BigDecimal score = buyListVo.getAmount();
                    //redisTemplate.opsForZSet().add(MATCH_SELL_ORDERS, buyListVo.getPlatformOrder(), score.doubleValue());
                    if(tryAddToZSet(MATCH_SELL_ORDERS, buyListVo.getPlatformOrder(), score.doubleValue(),3, 100)){
                        log.info("买入金额列表, 存储订单id到Redis zset成功, key:{}, orderId: {}", MATCH_SELL_ORDERS, buyListVo.getPlatformOrder());
                    } else {
                        log.error("买入金额列表, 存储订单id到Redis zset失败, key:{}, orderId: {}", MATCH_SELL_ORDERS, buyListVo.getPlatformOrder());
                    }

                } else {
                    //存储订单详情失败
                    log.error("买入金额列表, 存储订单详情到Redis哈希表失败, key:{}, 订单详情: {}", orderDetailsKey, orderDetailsMap);
                }
            } else {
                //存储订单id失败
                log.error("买入金额列表, 存储订单id到redis失败, redisKey:{}, 订单号: {}", orderDetailsKey, buyListVo.getPlatformOrder());
            }
        } else {
            log.error("买入金额列表, 将买入金额列表订单添加到redis失败, 已经存在该key, key:{}, buyListVo: {}", orderDetailsKey, buyListVo);
        }
    }


    /**
     * 使用 Redis 列表存储订单 ID
     *
     * @param listKey
     * @param orderId
     * @param maxAttempts
     * @param retryDelay
     * @param type
     * @return boolean
     */
    private boolean tryPush(String listKey, String orderId, int maxAttempts, long retryDelay, String type) {
        int attempt = 0;
        boolean success = false;
        while (!success && attempt < maxAttempts) {
            try {
                if ("1".equals(type)) {
                    //新的订单 右边添加(放在最后)
                    redisTemplate.opsForList().rightPush(listKey, orderId);
                } else {
                    // 被取消的订单 左边添加(放在最前)
                    redisTemplate.opsForList().leftPush(listKey, orderId);
                }

                success = true; // 如果没有抛出异常，则假设操作成功
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    log.error("尝试向Redis列表中添加订单ID失败达到最大次数，订单ID: {}, 错误: {}", orderId, e.getMessage());
                    return false; // 可以选择抛出异常或返回失败状态
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelay); // 等待一段时间后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    return false;
                }
            }
        }
        return success;
    }


    /**
     * 添加订单详情
     *
     * @param hashKey
     * @param map
     * @param maxAttempts
     * @param retryDelay
     * @return boolean
     */
    private boolean tryPutAllHash(String hashKey, Map<String, Object> map, int maxAttempts, long retryDelay) {
        int attempt = 0;
        boolean success = false;
        while (!success && attempt < maxAttempts) {
            try {
                redisTemplate.opsForHash().putAll(hashKey, map);
                success = true; // 如果没有抛出异常，则假设操作成功
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    log.error("尝试向Redis哈希表中添加数据失败达到最大次数，哈希表Key: {}, 错误: {}", hashKey, e.getMessage());
                    return false; // 可以选择抛出异常或返回失败状态
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelay); // 等待一段时间后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    return false;
                }
            }
        }
        return success;
    }

    private boolean tryAddToZSet(String key, Object value, double score,  int maxAttempts, long retryDelay) {
        int attempt = 0;
        boolean success = false;
        while (!success && attempt < maxAttempts) {
            try {
                redisTemplate.opsForZSet().add(key, value, score);
                success = true; // 如果没有抛出异常，则假设操作成功
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    log.error("尝试向Redis zset 添加数据失败达到最大次数，Key: {}, value:{}, 错误: {}", key, value, e.getMessage());
                    return false; // 可以选择抛出异常或返回失败状态
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelay); // 等待一段时间后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    return false;
                }
            }
        }
        return success;
    }

    /**
     * 获取单个订单的详情
     *
     * @param platformOrder 订单ID
     * @return {@link BuyListVo} 订单详情
     */
    public BuyListVo getOrderDetails(String platformOrder) {
        String orderDetailsKey = RedisKeys.ORDER_DETAILS + platformOrder;

        // 从Redis哈希表中获取订单详情
        Map<Object, Object> orderDetailsMap = redisTemplate.opsForHash().entries(orderDetailsKey);

        // 将Map转换回BuyListVo对象
        if (orderDetailsMap != null && !orderDetailsMap.isEmpty()) {
            return JSON.parseObject(JSON.toJSONString(orderDetailsMap), BuyListVo.class);
        }

        // 如果没有找到订单，返回null
        return null;
    }

    /**
     * 更新订单详情
     *
     * @param buyListVo
     */
    public void updateOrderDetails(BuyListVo buyListVo) {
        String orderDetailsKey = RedisKeys.ORDER_DETAILS + buyListVo.getPlatformOrder();

        try {
            redisTemplate.opsForHash().putAll(orderDetailsKey, JSON.parseObject(JSON.toJSONString(buyListVo), Map.class));
        } catch (Exception e) {
            log.error("将买入金额列表订单更新redis失败, e: {}", e);
            //抛出异常了 可能没有写入成功, 进行重试操作
            redisTemplate.opsForHash().putAll(orderDetailsKey, JSON.parseObject(JSON.toJSONString(buyListVo), Map.class));
        }

    }

    /**
     * 删除订单
     *
     * @param platformOrder
     */
    public void deleteOrder(String platformOrder) {
        // 从 Redis 列表中移除订单 ID
        log.info("从 Redis 列表中移除订单 ID: {}", platformOrder);
        Long remove = redisTemplate.opsForList().remove(RedisKeys.ORDERS_LIST, 0, platformOrder);

        if (remove == null || remove < 1) {
            Long remove2 = redisTemplate.opsForList().remove(RedisKeys.ORDERS_LIST, 0, platformOrder);

            if (remove2 == null || remove2 < 1) {
                Long remove3 = redisTemplate.opsForList().remove(RedisKeys.ORDERS_LIST, 0, platformOrder);
            }
        }

        // 从哈希表中删除订单详情
        String orderDetailsKey = RedisKeys.ORDER_DETAILS + platformOrder;

        log.info("从 Redis 列表中移除订单 详情: {}", orderDetailsKey);
        Boolean delete = redisTemplate.delete(orderDetailsKey);

        if (delete == null || !delete) {
            Boolean delete2 = redisTemplate.delete(orderDetailsKey);

            if (delete2 == null || !delete2) {
                Boolean delete3 = redisTemplate.delete(orderDetailsKey);
            }
        }

        // 从匹配中的卖出订单列表删除
        log.info("从 Redis zset中移除订单 ID: {}", platformOrder);
        redisTemplate.opsForZSet().remove(MATCH_SELL_ORDERS, platformOrder);
    }


    /**
     * 从redis里面获取买入金额列表 分页条件查询
     *
     * @param buyListReq
     * @return {@link PageReturn}<{@link BuyListVo}>
     */
    public PageReturn<BuyListVo> getBuyList(BuyListReq buyListReq) {

        // 固定最多显示的记录数
        final int maxRecords = 200;

        // 从 Redis 获取前200个订单ID
        List<String> allOrderIds = redisTemplate.opsForList().range(RedisKeys.ORDERS_LIST, 0, maxRecords - 1);

        // 获取订单详细信息并应用筛选条件
        List<BuyListVo> filteredOrders = new ArrayList<>();
        for (String orderId : allOrderIds) {

            // 从Redis哈希表中获取订单详情
            Map<Object, Object> orderDetailsMap = redisTemplate.opsForHash().entries(RedisKeys.ORDER_DETAILS + orderId);

            // 将Map转换回BuyListVo对象
            if (orderDetailsMap != null && !orderDetailsMap.isEmpty()) {
                BuyListVo buyListVo = JSON.parseObject(JSON.toJSONString(orderDetailsMap), BuyListVo.class);

                // 应用筛选条件
                if (buyListVo != null && meetsCriteria(buyListVo, buyListReq)) {
                    filteredOrders.add(buyListVo);
                }
            }

            // 如果已经收集足够的数据，则无需继续 (200条)
            if (filteredOrders.size() >= maxRecords) {
                break;
            }
        }

        // 构建结果对象
        PageReturn<BuyListVo> pageReturn = new PageReturn<>();
        pageReturn.setPageNo(1L); // 页码固定为1，因为不做分页
        pageReturn.setPageSize((long) filteredOrders.size()); // 实际返回的记录数
        pageReturn.setList(filteredOrders); // 符合条件的订单
        pageReturn.setTotal((long) filteredOrders.size()); // 总记录数，这里和pageSize相同

        return pageReturn;
    }


    /**
     * 动态查询条件 支付类型 最小金额 最大金额
     *
     * @param buyListVo
     * @param criteria
     * @return boolean
     */
    private boolean meetsCriteria(BuyListVo buyListVo, BuyListReq criteria) {

        boolean meetsAmountCriteria = true; // 默认满足条件
        boolean memberAmountLimit = true;

        // 检查最小金额条件
        if (criteria.getMinimumAmount() != null) {
            meetsAmountCriteria = buyListVo.getAmount().compareTo(criteria.getMinimumAmount()) >= 0;
        }

        // 检查最大金额条件
        if (criteria.getMaximumAmount() != null) {
            meetsAmountCriteria = meetsAmountCriteria && buyListVo.getAmount().compareTo(criteria.getMaximumAmount()) <= 0;
        }

        return meetsAmountCriteria && memberAmountLimit;
    }

    /**
     * 获取redis 字符串值
     *
     * @param key
     * @return {@link String}
     */
    public String getValueFromRedisIfExists(String key) {
        // 检查键是否存在
        Boolean exists = redisTemplate.hasKey(key);
        if (exists != null && exists) {
            // 键存在，获取值
            return (String) redisTemplate.opsForValue().get(key);
        } else {
            // 键不存在
            return null;
        }
    }


    /**
     * 更新会员最后的登录ip
     *
     * @param memberId
     * @param ip
     */
    public void updateMemberLastLoginIp(String memberId, String ip) {
        String key = "memberLoginIps"; // 所有会员的登录IP存储在这个哈希表中
        redisTemplate.opsForHash().put(key, memberId, ip);
    }


    /**
     * 根据会员ID获取最后登录的IP地址。
     *
     * @param memberId 会员的ID。
     * @return 最后登录的IP地址，如果没有找到则返回null。
     */
    public String getMemberLastLoginIp(String memberId) {
        String key = "memberLoginIps"; // 哈希表的键名，存储所有会员的登录IP
        return (String) redisTemplate.opsForHash().get(key, memberId);
    }



    /**
     * 操作redis 添加重试机制
     *
     * @param redisOperation
     * @param maxRetries     最多重试次数
     * @param delay          重试之间 间隔时间
     * @return boolean
     */
    public boolean retryTemplate(Runnable redisOperation, int maxRetries, long delay) {
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
     * 最后一次操作IP是否存在
     *
     * @param memberId
     * @return
     */
    public Boolean existLastIp(String memberId) {
        return redisTemplate.hasKey(MEMBER_LAST_IP + memberId);
    }

    /**
     * 刷新最后一次操作IP
     *
     * @param memberId
     * @param ip
     */
    public void refreshLastIp(String memberId, String ip) {
        redisTemplate.opsForValue().set(MEMBER_LAST_IP + memberId, ip, 30, TimeUnit.MINUTES);
    }

    /**
     * 订单支付OSR失败次数计数
     *
     * @param orderNo
     * @return
     */
    public Long countOsrOrderPayFail(String orderNo){
        String key = RedisKeys.ORDER_PAY_OSR_FAIL + orderNo;
        // 递增失败次数
        Long count = redisTemplate.opsForValue().increment(key);

        // 设置失败次数的过期时间（如果是第一次失败）
        if (count == 1L) {
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }
        return count;
    }

    /**
     * 获取支付OSR失败次数
     *
     * @param orderNo
     * @return
     */
    public Long getOsrOrderPayFailCount(String orderNo) {
        String key = RedisKeys.ORDER_PAY_OSR_FAIL + orderNo;
        // 递增失败次数
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Long.valueOf(value.toString());
    }

    /**
     * 获取进行中的订单号
     * @param memberId
     * @return
     */
    public List<String> getMemberProcessingOrder(String memberId){
        String key = String.format(RedisKeys.MEMBER_PROCESSING_ORDER, memberId);
        Set<String> members = redisTemplate.opsForSet().members(key);
        if(ObjectUtils.isEmpty(members)){
            return Collections.emptyList();
        }
        return new ArrayList<>(members);
    }

    public void setCreditScoreConfig(String key, String value){
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
    }

    public String getCreditScoreConfig(String key){
        Object o = redisTemplate.opsForValue().get(key);
        if(o == null){
            return null;
        }
        return String.valueOf(o);
    }

    public void delCreditScoreConfig(String key){
        redisTemplate.delete(key);
    }


    /**
     * 首页同步开关
     *
     * @param memberId
     * @return
     */
    public Boolean expireProcessingSync(String memberId) {
        return redisTemplate.hasKey(PROCESSING_ORDER_SYNC_SWITCH + memberId);
    }

    /**
     * 设置首页同步开关
     *
     * @param memberId
     * @return
     */
    public void setProcessingSync(String memberId) {
        redisTemplate.opsForValue().set(PROCESSING_ORDER_SYNC_SWITCH + memberId, "1" , 1, TimeUnit.HOURS);
    }

    /**
     * 同步卖出匹配订单历史数据
     */
    public void syncHistoryMatchSellOrder() {
        Long size = redisTemplate.opsForList().size(ORDERS_LIST);
        List<String> allOrderIds = redisTemplate.opsForList().range(RedisKeys.ORDERS_LIST, 0, size);
        log.info("买入列表zset历史数据同步...");
        long count = 0;
        for (String orderId : allOrderIds) {
            // 从Redis哈希表中获取订单详情
            Map<Object, Object> orderDetailsMap = redisTemplate.opsForHash().entries(RedisKeys.ORDER_DETAILS + orderId);
            // 将Map转换回BuyListVo对象
            if (orderDetailsMap != null && !orderDetailsMap.isEmpty()) {
                BuyListVo buyListVo = JSON.parseObject(JSON.toJSONString(orderDetailsMap), BuyListVo.class);
                BigDecimal score = buyListVo.getAmount();
                //redisTemplate.opsForZSet().add(MATCH_SELL_ORDERS, buyListVo.getPlatformOrder(), score.doubleValue());
                log.info("买入列表zset历史数据同步, orderId:{}", orderId);
                redisTemplate.opsForZSet().add(MATCH_SELL_ORDERS, orderId, score.doubleValue());
                count++;
            }
        }
        log.info("买入列表zset历史数据同步, 本次同步数量:{}", count);
    }


    /**
     * 获取会员当日取消匹配次数
     *
     * @param userId 用户ID
     * @return 返回会员当日取消匹配次数
     */
    public int canCancel(Long userId) {
        String redisKey = getRedisKey(userId);
        Object cancelCountObj = redisTemplate.opsForValue().get(redisKey);
        int cancelCount = cancelCountObj != null ? (int) cancelCountObj : 0;

        // 返回会员当日取消匹配次数
        return cancelCount;
    }

    /**
     * 增加当日取消匹配次数，过期时间为凌晨
     *
     * @param userId 用户ID
     */
    public void recordCancel(Long userId) {
        String redisKey = getRedisKey(userId);

        // 使用 Redis 的 increment 操作增加取消次数，并设置过期时间
        Long cancelCount = redisTemplate.opsForValue().increment(redisKey, 1);

        // 如果这是第一次设置键，则设置过期时间
        if (cancelCount != null && cancelCount == 1) {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redisTemplate.expire(redisKey, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取 Redis 键
     *
     * @param userId 用户ID
     * @return Redis 键
     */
    private String getRedisKey(Long userId) {
        LocalDate today = LocalDate.now();
        return RedisKeys.CANCEL_MATCHING_COUNT_TODAY + userId + ":" + today.toString();
    }

    /**
     * 获取距离午夜的秒数
     *
     * @return 距离午夜的秒数
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1);
        return midnight.atZone(ZoneId.systemDefault()).toEpochSecond() - LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public Map<String, String> getKycAutoCompleteHash() {
        return (Map<String, String>) redisTemplate.opsForHash().entries(RedisKeys.KYC_AUTO_COMPLETE_HASH);
    }

    public void addKycAutoCompleteHash(String name, String value) {
        redisTemplate.opsForHash().put(RedisKeys.KYC_AUTO_COMPLETE_HASH, name , value);
    }

    public void delKycAutoCompleteHash(String name) {
        redisTemplate.opsForHash().delete(RedisKeys.KYC_AUTO_COMPLETE_HASH, name);
    }

    public void setKycAutoCompleteExpireTime(String key, Integer expireTime) {
        String expireKey = "autoCompleteExpireTime:" + key;
        redisTemplate.opsForValue().set(expireKey, "1");
        redisTemplate.expire(expireKey, expireTime , TimeUnit.MINUTES);
    }

    public Boolean hasKycAutoCompleteExpireTime(String key) {
        String expireKey = "autoCompleteExpireTime:" + key;
        return redisTemplate.hasKey(expireKey);
    }

    public void delKycAutoCompleteExpireTime(String key) {
        String expireKey = "autoCompleteExpireTime:" + key;
        redisTemplate.delete(expireKey);
    }
}
