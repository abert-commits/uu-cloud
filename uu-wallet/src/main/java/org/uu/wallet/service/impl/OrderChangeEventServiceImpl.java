package org.uu.wallet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.uu.common.core.utils.StringUtils;
import org.uu.wallet.Enum.NotificationTypeEnum;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MatchPool;
import org.uu.wallet.entity.NotifyOrderStatusChangeMessage;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.service.*;

import static org.uu.common.redis.constants.RedisKeys.MEMBER_PROCESSING_ORDER;

@Service
@Slf4j
public class OrderChangeEventServiceImpl implements OrderChangeEventService {
    @Autowired
    private IPaymentOrderService paymentOrderService;
    @Autowired
    private IMatchPoolService matchPoolService;
    @Autowired
    private IMatchingOrderService matchingOrderService;
    @Autowired
    private ICollectionOrderService collectionOrderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    @Lazy
    private IBuyService buyService;
    @Autowired
    @Lazy
    private ISellService sellService;

    /**
     * 处理订单状所有状态变化的事件("卖出"除外)
     *
     * @param orderStatusChangeMessage
     */
    @Override
    public void process(NotifyOrderStatusChangeMessage orderStatusChangeMessage) {
        PaymentOrder paymentOrder;
        CollectionOrder collectionOrder;
        MatchPool matchPool = null;
        // 查询相关联的订单, 需要根据买入/卖出订单查询撮合订单, 因为撮合订单没有相关索引, 这里需要间接查询
        if (NotificationTypeEnum.NOTIFY_SELLER.getCode().equals(orderStatusChangeMessage.getType())) {
            String payOrderNo = orderStatusChangeMessage.getPlatformOrder();
            paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(payOrderNo);
            if (paymentOrder == null) {
                log.info("订单状态变化处理, 本次变化事件丢弃, 未查询到卖出订单:{}, 消息类型:{}", payOrderNo, orderStatusChangeMessage.getType());
                return;
            }
            String matchPoolNo = paymentOrder.getMatchOrder();
            if (!StringUtils.isEmpty(matchPoolNo)) {
                matchPool = matchPoolService.getMatchPoolOrderByOrderNo(matchPoolNo);
            }
            String matchingOrderNo = paymentOrder.getMatchingPlatformOrder();
            if (StringUtils.isEmpty(matchingOrderNo)) {
                log.info("订单状态变化处理, 本次变化事件丢弃, 卖出订单没有对应撮合订单:{}, 消息类型:{}", payOrderNo, orderStatusChangeMessage.getType());
                return;
            }
        } else {
            String buyOrderNo = orderStatusChangeMessage.getPlatformOrder();
            collectionOrder = collectionOrderService.getCollectionOrderByPlatformOrder(buyOrderNo);
            if (collectionOrder == null) {
                log.info("订单状态变化处理, 本次变化事件丢弃, 未查询到买入订单:{}, 消息类型:{}", buyOrderNo, orderStatusChangeMessage.getType());
                return;
            }
        }
    }

    /**
     * 卖出订单处理, 只处理此单一场景
     *
     * @param orderStatusChangeMsg
     */
    @Override
    public void processSellOrder(NotifyOrderStatusChangeMessage orderStatusChangeMsg) {
        String sellOrderNo = orderStatusChangeMsg.getPlatformOrder();
        String sellerKey = String.format(MEMBER_PROCESSING_ORDER, orderStatusChangeMsg.getMemberId());

        // 缓存会员进行中的订单
        log.info("订单状态变化处理, 卖出订单场景, 缓存会员进行中的订单-卖出:{}, memberId:{}", sellOrderNo, orderStatusChangeMsg.getMemberId());
        redisTemplate.opsForSet().add(sellerKey, sellOrderNo);

        // 其他业务可在此扩展
    }


    /**
     * 处理取消卖出订单(无撮合订单)
     *
     * @param orderStatusChangeMsg
     */
    @Override
    public void processCancelSellOrder(NotifyOrderStatusChangeMessage orderStatusChangeMsg) {
        String sellOrderNo = orderStatusChangeMsg.getPlatformOrder();
        String sellerKey = String.format(MEMBER_PROCESSING_ORDER, orderStatusChangeMsg.getMemberId());

        // 缓存会员进行中的订单
        log.info("订单状态变化处理, 取消订单, 移除会员进行中的订单-卖出:{}, memberId:{}", sellOrderNo, orderStatusChangeMsg.getMemberId());
        redisTemplate.opsForSet().remove(sellerKey, sellOrderNo);

    }
}
