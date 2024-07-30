package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.pay.dto.MatchingOrderDTO;
import org.uu.common.pay.dto.MatchingOrderInfoDTO;
import org.uu.common.pay.dto.RelationOrderDTO;
import org.uu.common.pay.dto.TradeManualConfigDTO;
import org.uu.common.pay.req.*;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.req.CancelOrderReq;
import org.uu.wallet.service.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingOrderServiceImpl extends ServiceImpl<MatchingOrderMapper, MatchingOrder> implements IMatchingOrderService {

    private final IPaymentOrderService paymentOrderService;
    private final CollectionOrderMapper collectionOrderMapper;
    private final AppealOrderMapper appealOrderMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final MemberInfoMapper memberInfoMapper;
    private final ITradeConfigService tradeConfigService;
    @Autowired
    private IAppealOrderService appealOrderService;

    @Override
    public MatchingOrder getMatchingOrderByCollection(String collectionOrder) {
        return lambdaQuery().eq(MatchingOrder::getCollectionPlatformOrder, collectionOrder).or().eq(MatchingOrder::getCollectionMerchantOrder, collectionOrder).one();
    }

    @Override
    public MatchingOrderInfoDTO getInfo(MatchingOrderIdReq req) {
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        matchingOrder = baseMapper.selectById(matchingOrder);
        PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(matchingOrder.getPaymentPlatformOrder());
        if (matchingOrder == null) return null;
        MatchingOrderDTO matchingOrderDTO = new MatchingOrderDTO();
        BeanUtils.copyProperties(matchingOrder, matchingOrderDTO);
        if (ObjectUtils.isNotEmpty(paymentOrder)) {
            matchingOrderDTO.setMobileNumber(paymentOrder.getMobileNumber());
        }
        if (matchingOrder.getStatus().equals(OrderStatusEnum.WAS_CANCELED.getCode())) {
            matchingOrderDTO.setRemark(matchingOrder.getCancellationReason());
        }
        // 查询是否开启人工审核
        TradeManualConfigDTO manualReview = tradeConfigService.manualReview();
        matchingOrderDTO.setIsManualReview(manualReview.getIsManualReview());
        // 查询随机码
        if(ObjectUtils.isNotEmpty(matchingOrder.getCollectionPlatformOrder())){
            CollectionOrder orderByOrderNo = collectionOrderMapper.getOrderByOrderNo(matchingOrder.getCollectionPlatformOrder());
            // 获取随机码
            String randomCode = orderByOrderNo.getRandomCode();
            matchingOrderDTO.setRandomCode(randomCode);
        }
        MatchingOrderInfoDTO matchingOrderInfoDTO = new MatchingOrderInfoDTO();
        BeanUtils.copyProperties(matchingOrderDTO, matchingOrderInfoDTO);
        String collectionOrderNo = matchingOrder.getCollectionPlatformOrder();
        CollectionOrder collectionOrderInfo = collectionOrderMapper.getOrderByOrderNo(collectionOrderNo);
        if(matchingOrder.getPayType().equals(PayTypeEnum.INDIAN_CARD.getCode()) || matchingOrder.getPayType().equals(PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())){
            // 查询银行卡信息
            matchingOrderInfoDTO.setBankName(collectionOrderInfo.getBankName());
            matchingOrderInfoDTO.setBankCardNumber(collectionOrderInfo.getBankCardNumber());
            matchingOrderInfoDTO.setBankCardOwner(collectionOrderInfo.getBankCardOwner());
            matchingOrderInfoDTO.setIfscCode(collectionOrderInfo.getIfscCode());
        }else{
            // 查询upi信息
            matchingOrderInfoDTO.setUpiId(collectionOrderInfo.getUpiId());
            matchingOrderInfoDTO.setUpiName(collectionOrderInfo.getUpiName());
        }
        return matchingOrderInfoDTO;

    }

//    public AppealOrderDTO appealDetail(MatchingOrderReq req){
//        List<AppealOrder> list = appealOrderService.lambdaQuery().eq(AppealOrder::getWithdrawOrderNo,req.getCollectionPlatformOrder()).or().eq(AppealOrder::getRechargeOrderNo,req.getPaymentPlatformOrder()).list();
//        AppealOrderDTO appealOrderDTO = new AppealOrderDTO();
//        if(list==null||list.size()<1) return null;
//        BeanUtils.copyProperties(list.get(0),appealOrderDTO);
//        return appealOrderDTO;
//    }


    @Override
    public MatchingOrderDTO update(MatchingOrderReq req) {
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        baseMapper.updateById(matchingOrder);
        MatchingOrderDTO matchingOrderDTO = new MatchingOrderDTO();
        BeanUtils.copyProperties(matchingOrder, matchingOrderDTO);
        return matchingOrderDTO;

    }


    @Override
    public MatchingOrderDTO getMatchingOrderTotal(MatchingOrderReq req) {
        QueryWrapper<MatchingOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "sum(order_submit_amount) as orderSubmitAmount",
                "sum(order_actual_amount) as orderActualAmount"

        );
        if (req.getId() != null) {
            queryWrapper.eq("id", req.getId());
        }
        if (!StringUtils.isEmpty(req.getCollectionPlatformOrder())) {
            queryWrapper.eq("collection_platform_order", req.getCollectionPlatformOrder());
        }

        if (!StringUtils.isEmpty(req.getPaymentPlatformOrder())) {
            queryWrapper.eq("payment_platform_order", req.getPaymentPlatformOrder());
        }
        if (!StringUtils.isEmpty(req.getStatus())) {
            queryWrapper.eq("status", req.getStatus());
        }
        if (!StringUtils.isEmpty(req.getCreateTimeStart())) {
            queryWrapper.ge("create_time", req.getCreateTimeStart());
        }
        if (!StringUtils.isEmpty(req.getCreateTimeEnd())) {
            queryWrapper.le("create_time", req.getCreateTimeEnd());
        }
        if (!StringUtils.isEmpty(req.getPaymentTimeStart())) {
            queryWrapper.ge("payment_time", req.getPaymentTimeStart());
        }
        if (!StringUtils.isEmpty(req.getPaymentTimeEnd())) {
            queryWrapper.le("payment_time", req.getPaymentTimeEnd());
        }
        if (!StringUtils.isEmpty(req.getCompletionTimeStart())) {
            queryWrapper.ge("completion_time", req.getCompletionTimeStart());
        }
        if (!StringUtils.isEmpty(req.getCompletionTimeEnd())) {
            queryWrapper.le("completion_time", req.getCompletionTimeEnd());
        }

        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page, queryWrapper);
        List<Map<String, Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<MatchingOrderDTO> list = jsonArray.toJavaList(MatchingOrderDTO.class);
        MatchingOrderDTO matchingOrderDTO = list.get(0);

        return matchingOrderDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchingOrderDTO nopay(MatchingOrderAppealReq req) {
        String updateBy = UserContext.getCurrentUserName();
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        matchingOrder = baseMapper.selectById(matchingOrder);
        log.info("BI后台未支付,卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
        AppealOrder appealOrder = appealOrderMapper.queryAppealOrderByNo(matchingOrder.getPaymentPlatformOrder());

        matchingOrder.setUpdateBy(updateBy);
        matchingOrder.setUpdateTime(LocalDateTime.now());
        matchingOrder.setRemark(req.getRemark());
        matchingOrder.setCompletedBy(updateBy);
        matchingOrder.setAppealTime(LocalDateTime.now(ZoneId.systemDefault()));
        matchingOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));

        CancelOrderReq cancelOrderReq = new CancelOrderReq();
        cancelOrderReq.setPlatformOrder(matchingOrder.getCollectionPlatformOrder());
        MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(matchingOrder.getCollectionMemberId());
        CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(matchingOrder.getCollectionPlatformOrder());
        collectionOrder.setUpdateBy(updateBy);
        collectionOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        collectionOrder.setRemark(req.getRemark());


        PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(matchingOrder.getPaymentPlatformOrder());
        paymentOrder.setUpdateBy(updateBy);
        paymentOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        paymentOrder.setRemark(req.getRemark());


        if (!ObjectUtils.isEmpty(appealOrder) && appealOrder.getAppealStatus().equals(1)) {
            log.info("未支付更新申诉订单");
            appealOrder.setAppealStatus(3);
            appealOrderMapper.updateById(appealOrder);
            matchingOrder.setAppealReviewBy(UserContext.getCurrentUserName());
            matchingOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));

            paymentOrder.setAppealReviewBy(UserContext.getCurrentUserName());
            paymentOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));

            collectionOrder.setAppealReviewBy(UserContext.getCurrentUserName());
            collectionOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
        }
        baseMapper.updateById(matchingOrder);
        collectionOrderMapper.updateById(collectionOrder);
        paymentOrderService.updateById(paymentOrder);

        if (appealOrder != null) {
            // 变更会员信用分
            appealOrderService.changeCreditScore(Boolean.FALSE, collectionOrder.getMemberId(), paymentOrder.getMemberId(), appealOrder);
        } else {
            log.info("BI后台未支付, 申诉单为空无需更新信用分, 卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
        }


        MatchingOrderDTO matchingOrderDTO = new MatchingOrderDTO();
        BeanUtils.copyProperties(matchingOrder, matchingOrderDTO);
        return matchingOrderDTO;


    }

    @Override
    public Map<String, String> getMatchMemberIdByPlatOrderIdList(List<String> platOrderIdList, boolean isBuy) {
        Map<String, String> matchMemberId = new HashMap<>();
        if (platOrderIdList.isEmpty()) {
            return matchMemberId;
        }
        LambdaQueryChainWrapper<MatchingOrder> queryChainWrapper = lambdaQuery();
        if (isBuy) {
            queryChainWrapper.in(MatchingOrder::getCollectionPlatformOrder, platOrderIdList);
        } else {
            queryChainWrapper.in(MatchingOrder::getPaymentPlatformOrder, platOrderIdList);
        }
        List<MatchingOrder> matchingOrders = baseMapper.selectList(queryChainWrapper.getWrapper());
        if (isBuy) {
            matchMemberId = matchingOrders.stream().collect(Collectors.toMap(MatchingOrder::getCollectionPlatformOrder, MatchingOrder::getPaymentMemberId));
        } else {
            matchingOrders.sort(Comparator.comparing(MatchingOrder::getCreateTime).reversed());
            matchMemberId = matchingOrders.stream().collect(Collectors.toMap(MatchingOrder::getPaymentPlatformOrder, MatchingOrder::getCollectionMemberId, (k1, k2) -> k1));
        }
        return matchMemberId;
    }

    /**
     * 根据买入订单、卖出订单批量汇总查询关联的撮合订单ID列表
     *
     * @param buyOrderIds
     * @param sellOrderIds
     * @return
     */
    @Override
    public Map<String, String> getMatchOrderIdsByPlatOrderId(List<String> buyOrderIds, List<String> sellOrderIds) {
        Set<String> orderSet = new HashSet<>();
        Set<String> buySet = new HashSet<>();
        Set<String> sellSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(buyOrderIds)) {
            List<MatchingOrder> buyList = lambdaQuery().in(MatchingOrder::getCollectionPlatformOrder, buyOrderIds)
                    .select(MatchingOrder::getPlatformOrder)
                    .list();
            if (!CollectionUtils.isEmpty(buyList)) {
                buySet = buyList.stream().map(MatchingOrder::getPlatformOrder).collect(Collectors.toSet());
                orderSet.addAll(buySet);
            }
        }

        if (!CollectionUtils.isEmpty(sellOrderIds)) {
            List<MatchingOrder> sellList = lambdaQuery().in(MatchingOrder::getPaymentPlatformOrder, sellOrderIds)
                    .select(MatchingOrder::getPlatformOrder)
                    .list();
            if (!CollectionUtils.isEmpty(sellList)) {
                sellSet = sellList.stream().map(MatchingOrder::getPlatformOrder).collect(Collectors.toSet());
                orderSet.addAll(sellSet);
            }
        }
        Map<String, String> orderMap = Maps.newHashMapWithExpectedSize(orderSet.size());
        if (CollectionUtils.isEmpty(orderSet)) {
            return orderMap;
        }
        for (String platformOrder : orderSet) {
            if (buySet.contains(platformOrder) && sellSet.contains(platformOrder)) {
                orderMap.put(platformOrder, RiskOrderTypeEnum.ALL.getCode());
            } else if (buySet.contains(platformOrder)) {
                orderMap.put(platformOrder, RiskOrderTypeEnum.COLLECTION.getCode());
            } else {
                orderMap.put(platformOrder, RiskOrderTypeEnum.PAYMENT.getCode());
            }
        }
        return orderMap;
    }

    /**
     * 标记订单为指定的tag
     *
     * @param riskTag
     * @param platformOrderTags
     */
    @Override
    @Transactional
    public void taggingOrders(String riskTag, Map<String, String> platformOrderTags) {
        if (RiskTagEnum.getNameByCode(riskTag) == null) {
            return;
        }
        if (CollectionUtils.isEmpty(platformOrderTags)) {
            return;
        }
        Map<String, List<String>> typeIdMap = new HashMap<>();
        platformOrderTags.keySet().forEach(orderId -> {
            String riskOrderType = platformOrderTags.get(orderId);
            if (!typeIdMap.containsKey(riskOrderType)) {
                typeIdMap.put(riskOrderType, new ArrayList<>());
            }
            typeIdMap.get(riskOrderType).add(orderId);
        });
        for (Map.Entry<String, List<String>> entry : typeIdMap.entrySet()) {
            log.info("订单标记, 修改DB撮合订单状态, riskOrderType:{}, orderIds:{}", entry.getKey(), entry.getValue());
            LambdaUpdateChainWrapper<MatchingOrder> updateWrapper = lambdaUpdate().in(MatchingOrder::getPlatformOrder, entry.getValue());
            if (RiskTagEnum.BLACK_IP.getCode().equals(riskTag)) {
                updateWrapper.set(MatchingOrder::getRiskTagBlack, 1);
            }
            if (RiskTagEnum.ORDER_TIME_OUT.getCode().equals(riskTag)) {
                updateWrapper.set(MatchingOrder::getRiskTagTimeout, 1);
            }
            updateWrapper.set(MatchingOrder::getRiskOrderType, entry.getKey());
            updateWrapper.update();
        }
    }

    @SneakyThrows
    @Override
    public Page<RelationOrderDTO> relationOrderList(RelationshipOrderReq req) {

        Page<RelationOrderDTO> pageInfo = new Page<>();
        long page = (req.getPageNo() - 1) * req.getPageSize();
        long size = req.getPageSize();

//        // 查询商户信息
//        CompletableFuture<List<RelationOrderDTO>> listFuture = CompletableFuture.supplyAsync(() -> {
//            return matchingOrderMapper.selectMyPage(page, size, req);
//        });
//        // 查询商户信息
//        CompletableFuture<Long> countFuture = CompletableFuture.supplyAsync(() -> {
//            return matchingOrderMapper.count(req);
//        });
//
//        CompletableFuture<Void> allFutures = CompletableFuture.allOf(listFuture, countFuture);
//        allFutures.get();
//        List<RelationOrderDTO> resultList = listFuture.get();
//        for (RelationOrderDTO item : resultList) {
//            if (org.apache.commons.lang3.StringUtils.isNotBlank(item.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(item.getMerchantCode()) &&
//                    item.getMemberId().contains(item.getMerchantCode())) {
//                String externalMemberId = item.getMemberId().substring(item.getMerchantCode().length());
//                item.setMemberId(externalMemberId);
//            }
//        }
//        pageInfo.setRecords(resultList);
//        pageInfo.setTotal(countFuture.get());
        return pageInfo;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean manualReview(MatchingOrderManualReq req, ISellService sellService) {
        // 判断开关状态
        TradeManualConfigDTO manualReview = tradeConfigService.manualReview();
        if (manualReview.getIsManualReview() == 0) {
            return true;
        }
        // 获取撮合订单信息
        MatchingOrder matchingOrder = baseMapper.selectById(req.getId());
        if (OrderStatusEnum.SUCCESS.getCode().equals(matchingOrder.getStatus()) || OrderStatusEnum.WAS_CANCELED.getCode().equals(matchingOrder.getStatus())) {
            // 排除不需要处理的状态
            log.error("人工审核处理订单, 该订单无需处理, orderNo: {}, status:{}", matchingOrder.getPlatformOrder(), matchingOrder.getStatus());
            return true;
        }
        try {

            if (req.getIsPass().equals(ManualReviewStatus.PASS.getCode())) {
                // 人工审核暂时无通过操作
                // 审核通过确认订单
//                RestResult<BuyCompletedVo> buyCompletedVoRestResult = sellService.transactionSuccessHandler(matchingOrder.getPaymentPlatformOrder(), Long.parseLong(matchingOrder.getPaymentMemberId()), null, null, "2", null);
//                if (!buyCompletedVoRestResult.getCode().equals(ResultCode.SUCCESS.getCode())) {
//                    throw new Exception("人工审核确认订单失败: paymentOrder:{" + matchingOrder.getPaymentPlatformOrder() + "} , paymentMemberId:{" + matchingOrder.getPaymentMemberId() + "}" + ", buyCompletedVoRestMsg:{" + buyCompletedVoRestResult.getMsg() + "}");
//                }
            } else {
                // 获取拒绝原因
                String refuseReason = req.getReasonRemark();
                String nameByCode = RefuseReasonEnum.getNameByCode(req.getRefuseReason());
                if (Objects.nonNull(nameByCode)) {
                    refuseReason = nameByCode;
                }
                // 审核不通过 取消订单
                CancelOrderReq cancelOrderReq = new CancelOrderReq();
                cancelOrderReq.setPlatformOrder(matchingOrder.getCollectionPlatformOrder());
                MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(matchingOrder.getCollectionMemberId());
            }
            return true;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("人工审核处理失败: req: {}, e: {}", req, e.getMessage());
            return false;
        }
    }
}