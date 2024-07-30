package org.uu.wallet.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TradeIpBlackListPageDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.common.pay.req.TradeIpBlackListReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.OrderTypeEnum;
import org.uu.wallet.Enum.RiskTagEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.TradeIpBlacklistMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易IP黑名单表，用于存储不允许进行交易的IP地址 服务实现类
 * </p>
 *
 * @author
 * @since 2024-02-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeIpBlacklistServiceImpl extends ServiceImpl<TradeIpBlacklistMapper, TradeIpBlacklist> implements ITradeIpBlacklistService {

    @Autowired
    private WalletMapStruct walletMapStruct;
    @Lazy
    @Autowired
    private IMatchingOrderService matchingOrderService;
    @Lazy
    @Autowired
    private IPaymentOrderService paymentOrderService;
    @Lazy
    @Autowired
    private IMemberInfoService memberInfoService;
    @Lazy
    @Autowired
    private RabbitMQService rabbitMQService;
    @Lazy
    @Autowired
    private ICollectionOrderService collectionOrderService;
    private final IMemberBlackService memberBlackService;

    @Override
    public PageReturn<TradeIpBlackListPageDTO> listPage(TradeIpBlackListReq req) {
        Page<TradeIpBlacklist> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<TradeIpBlacklist> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(TradeIpBlacklist::getId);
        if (!StringUtils.isEmpty(req.getIp())) {
            lambdaQuery.eq(TradeIpBlacklist::getIpAddress, req.getIp());
        }
        lambdaQuery.eq(TradeIpBlacklist::getDeleted, GlobalConstants.STATUS_OFF);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<TradeIpBlacklist> records = page.getRecords();
        List<TradeIpBlackListPageDTO> list = walletMapStruct.tradeIpBlackListToDto(records);
        return PageUtils.flush(page, list);
    }

    @Override
    public RestResult save(TradeIpBlackListReq req) {
        TradeIpBlacklist tradeIpBlacklist = new TradeIpBlacklist();
        String userName = UserContext.getCurrentUserName();
        BeanUtils.copyProperties(req, tradeIpBlacklist);
        tradeIpBlacklist.setIpAddress(req.getIp());
        tradeIpBlacklist.setReason(req.getRemark());
        if (ObjectUtils.isNotEmpty(req.getId())) {
            tradeIpBlacklist.setUpdateBy(userName);
            int result = baseMapper.updateById(tradeIpBlacklist);
            if(result > 0 && "0".equals(req.getStatus())){
                TradIpBlackMessage tradIpBlackMessage = new TradIpBlackMessage();
                tradIpBlackMessage.setType(req.getStatus());
                tradIpBlackMessage.setTradeIpBlacklist(tradeIpBlacklist);
                rabbitMQService.sendTradeIpBlackAddMessage(tradIpBlackMessage);
            }else if(result > 0 && "1".equals(req.getStatus())){
                TradIpBlackMessage tradIpBlackMessage = new TradIpBlackMessage();
                tradIpBlackMessage.setType("1");
                tradIpBlackMessage.setAutoFlag("2");
                tradeIpBlacklist.setReason("【手动禁用IP】" + req.getRemark());
                tradIpBlackMessage.setTradeIpBlacklist(tradeIpBlacklist);
                rabbitMQService.sendTradeIpBlackAddMessage(tradIpBlackMessage);
            }
        } else {
            LambdaQueryChainWrapper<TradeIpBlacklist> lambdaQuery = lambdaQuery();
            lambdaQuery.eq(TradeIpBlacklist::getIpAddress, req.getIp());
            lambdaQuery.eq(TradeIpBlacklist::getDeleted, GlobalConstants.STATUS_OFF);
            List<TradeIpBlacklist> list = baseMapper.selectList(lambdaQuery.getWrapper());
            if (list.size() > 0) {
                return RestResult.failed("Repeated addition");
            }
            tradeIpBlacklist.setCreateBy(userName);
            tradeIpBlacklist.setUpdateBy(userName);
            tradeIpBlacklist.setReason("【手动禁用IP】" + req.getRemark());
            Integer result = baseMapper.insert(tradeIpBlacklist);
            if(result > 0){
                TradIpBlackMessage tradIpBlackMessage = new TradIpBlackMessage();
                tradIpBlackMessage.setType("1");
                tradIpBlackMessage.setAutoFlag("2");
                tradIpBlackMessage.setTradeIpBlacklist(tradeIpBlacklist);
                rabbitMQService.sendTradeIpBlackAddMessage(tradIpBlackMessage);
            }
        }
        return RestResult.ok();
    }

    @Override
    public boolean del(String id) {
        TradeIpBlacklist tradeIpBlacklist = new TradeIpBlacklist();
        tradeIpBlacklist.setId(Long.valueOf(id));
        tradeIpBlacklist.setDeleted(GlobalConstants.STATUS_ON);
        int result = baseMapper.updateById(tradeIpBlacklist);
        tradeIpBlacklist = baseMapper.selectById(Long.valueOf(id));
        if(result > 0){
            TradIpBlackMessage tradIpBlackMessage = new TradIpBlackMessage();
            tradIpBlackMessage.setType("0");
            tradIpBlackMessage.setTradeIpBlacklist(tradeIpBlacklist);
            rabbitMQService.sendTradeIpBlackAddMessage(tradIpBlackMessage);
        }
        return result > 0;
    }

    /**
     * 查看交易ip是否在黑名单中
     *
     * @param ip
     * @return {@link Boolean}
     */
    @Override
    public Boolean isIpBlacklisted(String ip) {
        LambdaQueryWrapper<TradeIpBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeIpBlacklist::getIpAddress, ip)
                .eq(TradeIpBlacklist::getDeleted, 0)
                .eq(TradeIpBlacklist::getStatus, 1); // 确保黑名单条目是生效状态


        return this.count(queryWrapper) > 0;
    }

    /**
     * 添加Ip黑名单回调方法
     *
     * @param tradIpBlackMessage
     * @return
     */
    @Override
    public void addBlackIpCallback(TradIpBlackMessage tradIpBlackMessage) {
        TradeIpBlacklist tradeIpBlacklist = tradIpBlackMessage.getTradeIpBlacklist();
        // 1.禁用会员
        Set<String> allMemberIds = new HashSet<>();
        List<PaymentOrder> paymentOrders = paymentOrderService.getPaymentOrderByByIp(tradeIpBlacklist.getIpAddress());
        if (!CollectionUtils.isEmpty(paymentOrders)) {
            Set<String> paymentMemberIds = paymentOrders.stream().map(PaymentOrder::getMemberId).collect(Collectors.toSet());
            allMemberIds.addAll(paymentMemberIds);
        }

        List<CollectionOrder> collectOrders = collectionOrderService.getCollectOrderByByIp(tradeIpBlacklist.getIpAddress());
        if (!CollectionUtils.isEmpty(collectOrders)) {
            Set<String> collectMemberIds = collectOrders.stream().map(CollectionOrder::getMemberId).collect(Collectors.toSet());
            allMemberIds.addAll(collectMemberIds);
        }

        List<String> loginMemberIds = memberInfoService.getMembersByByLoginIp(tradeIpBlacklist.getIpAddress());
        if (!CollectionUtils.isEmpty(loginMemberIds)) {
            allMemberIds.addAll(new HashSet<>(loginMemberIds));
        }

        if (CollectionUtils.isEmpty(allMemberIds)) {
            log.info("添加交易黑名单, 关联会员列表为空, 无需处理");
            return;
        }

        if ("1".equals(tradIpBlackMessage.getType()) && "1".equals(tradIpBlackMessage.getAutoFlag())) {
            log.info("添加交易黑名单, 发送禁用关联会员消息, size:{}, members:{}", allMemberIds.size(), allMemberIds);
            allMemberIds.forEach(memberId -> rabbitMQService.sendMemberDisableMessage(memberId, "【系统自动禁用IP】" + tradeIpBlacklist.getReason()));
        } else if("1".equals(tradIpBlackMessage.getType()) && "2".equals(tradIpBlackMessage.getAutoFlag())){
            allMemberIds.forEach(memberId -> rabbitMQService.sendMemberDisableMessage(memberId, tradeIpBlacklist.getReason()));
        }else if("0".equals(tradIpBlackMessage.getType())){
            // 禁用黑名单
            allMemberIds.forEach(memberId -> {
                MemberBlackReq memberBlack = new MemberBlackReq();
                memberBlack.setMemberId(memberId);
                memberBlackService.removeBlack(memberBlack);
            });
        }

        // 2.标记订单为黑名单
        Map<String, String> sellOrderIdMap = new HashMap<>();
        Map<String, String> buyOrderIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(paymentOrders)) {
            List<String> sellOrderIds = paymentOrders.stream().map(PaymentOrder::getPlatformOrder).collect(Collectors.toList());
            sellOrderIds.forEach(sellOrderId -> sellOrderIdMap.put(sellOrderId, null));
        }

        if (!CollectionUtils.isEmpty(collectOrders)) {
            List<String> buyOrderIds = collectOrders.stream().map(CollectionOrder::getPlatformOrder).collect(Collectors.toList());
            buyOrderIds.forEach(buyOrderId -> buyOrderIdMap.put(buyOrderId, null));
        }

        Map<String, String> matchOrderIdMap = matchingOrderService.getMatchOrderIdsByPlatOrderId(new ArrayList<>(buyOrderIdMap.keySet()), new ArrayList<>(sellOrderIdMap.keySet()));

        String riskType = "1".equals(tradIpBlackMessage.getType()) ? RiskTagEnum.BLACK_IP.getCode() : RiskTagEnum.Normal.getCode();
        // 发送标记买入订单消息
        rabbitMQService.sendOrderTaggingMessage(OrderTaggingMessage.builder().riskType(riskType).orderType(OrderTypeEnum.COLLECTION.getCode()).platformOrderTags(buyOrderIdMap).build());
        // 发送标记卖出订单消息
        rabbitMQService.sendOrderTaggingMessage(OrderTaggingMessage.builder().riskType(riskType).orderType(OrderTypeEnum.PAYMENT.getCode()).platformOrderTags(sellOrderIdMap).build());
        // 发送标记撮合订单消息
        rabbitMQService.sendOrderTaggingMessage(OrderTaggingMessage.builder().riskType(riskType).orderType(OrderTypeEnum.MATCH.getCode()).platformOrderTags(matchOrderIdMap).build());
    }
}
