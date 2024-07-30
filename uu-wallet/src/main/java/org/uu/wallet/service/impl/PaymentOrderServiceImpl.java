package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.DurationCalculatorUtil;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.CollectionOrderIdReq;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.common.pay.req.PaymentOrderIdReq;
import org.uu.common.pay.req.PaymentOrderListPageReq;
import org.uu.common.web.exception.BizException;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.CollectionInfoMapper;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MatchingOrderMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.req.BuyListReq;
import org.uu.wallet.req.SellOrderListReq;
import org.uu.wallet.service.AsyncNotifyService;
import org.uu.wallet.service.IMatchPoolService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IPaymentOrderService;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.PaymentOrderInfoVo;
import org.uu.wallet.vo.SellOrderListVo;
import org.uu.wallet.vo.ViewSellOrderDetailsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements IPaymentOrderService {

    @Autowired
    private IMerchantInfoService merchantInfoService;

    private final RedisUtil redisUtil;

    @Autowired
    private IMatchPoolService matchPoolService;

    @Resource
    private PaymentOrderMapper paymentOrderMapper;
    @Resource
    private MatchingOrderMapper matchingOrderMapper;
    @Autowired
    private CollectionOrderMapper collectionOrderMapper;
    @Autowired
    private CollectionInfoMapper collectionInfoMapper;

    @Resource
    AsyncNotifyService asyncNotifyService;

    @Override
    @SneakyThrows
    public PageReturn<PaymentOrderListPageDTO> listPage(PaymentOrderListPageReq req) {

        Page<PaymentOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<PaymentOrder> lambdaQuery = lambdaQuery();
        // 排序
        if(ObjectUtils.isNotEmpty(req.getColumn())){
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }else{
            lambdaQuery.orderByDesc(PaymentOrder::getId);
        }
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<PaymentOrder>()
                .select("IFNULL(sum(amount),0) as amountTotal," +
                        "IFNULL(sum(actual_amount), 0) as actualAmountTotal," +
                        "IFNULL(sum(bonus),0) as bonusTotal," +
                        "IFNULL(sum(itoken_number),0) as ITokenTotal"
                ).lambda();


        if (ObjectUtils.isNotEmpty(req.getMemberType())) {
            lambdaQuery.eq(PaymentOrder::getMemberType, req.getMemberType());
            queryWrapper.eq(PaymentOrder::getMemberType, req.getMemberType());
        }

        //--动态查询 商户名称
        if (!StringUtils.isEmpty(req.getMerchantName())) {
            lambdaQuery.eq(PaymentOrder::getMerchantName, req.getMerchantName());
            queryWrapper.eq(PaymentOrder::getMerchantName, req.getMerchantName());
        }

        //--动态查询 币种
        if (!StringUtils.isEmpty(req.getCurrency())) {
            lambdaQuery.eq(PaymentOrder::getCurrency, req.getCurrency());
            queryWrapper.eq(PaymentOrder::getCurrency, req.getCurrency());
        }

        //--动态查询 utr
        if (!StringUtils.isEmpty(req.getUtr())) {
            lambdaQuery.eq(PaymentOrder::getUtr, req.getUtr());
            queryWrapper.eq(PaymentOrder::getUtr, req.getUtr());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.and(e -> e.or().eq(PaymentOrder::getMemberId, req.getMemberId()).or().eq(PaymentOrder::getMemberAccount, req.getMemberId()));
            queryWrapper.and(e -> e.or().eq(PaymentOrder::getMemberId, req.getMemberId()).or().eq(PaymentOrder::getMemberAccount, req.getMemberId()));
        }

        //--动态查询 商户订单号
        if (!StringUtils.isEmpty(req.getMerchantOrder())) {
            lambdaQuery.and(e-> e.eq(PaymentOrder::getMerchantOrder, req.getMerchantOrder()).or().eq(PaymentOrder::getMerchantCollectionOrder, req.getMerchantOrder()));
            queryWrapper.and(e-> e.eq(PaymentOrder::getMerchantOrder, req.getMerchantOrder()).or().eq(PaymentOrder::getMerchantCollectionOrder, req.getMerchantOrder()));
        }

        //--动态查询 平台订单号
        if (!StringUtils.isEmpty(req.getPlatformOrder())) {
            lambdaQuery.eq(PaymentOrder::getPlatformOrder, req.getPlatformOrder());
            queryWrapper.eq(PaymentOrder::getPlatformOrder, req.getPlatformOrder());
        }

        //--动态查询 支付状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            lambdaQuery.eq(PaymentOrder::getOrderStatus, req.getOrderStatus());
            queryWrapper.eq(PaymentOrder::getOrderStatus, req.getOrderStatus());
        }

        //--动态查询 回调状态
        if (!StringUtils.isEmpty(req.getTradeCallbackStatus())) {
            lambdaQuery.eq(PaymentOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
            queryWrapper.eq(PaymentOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
        }

        //--动态查询 提现时间开始
        if (!ObjectUtils.isEmpty(req.getCreateTimeStart())) {
            lambdaQuery.ge(PaymentOrder::getCreateTime, req.getCreateTimeStart());
            queryWrapper.ge(PaymentOrder::getCreateTime, req.getCreateTimeStart());
        }

        //--动态查询 提现结束
        if (!ObjectUtils.isEmpty(req.getCreateTimeEnd())) {
            lambdaQuery.le(PaymentOrder::getCreateTime, req.getCreateTimeEnd());
            queryWrapper.le(PaymentOrder::getCreateTime, req.getCreateTimeEnd());
        }

        //--动态查询 提现时间开始
        if (!ObjectUtils.isEmpty(req.getCompletionTimeStart())) {
            lambdaQuery.ge(PaymentOrder::getCompletionTime, req.getCompletionTimeStart());
            queryWrapper.ge(PaymentOrder::getCompletionTime, req.getCompletionTimeStart());
        }

        //--动态查询 提现结束
        if (!ObjectUtils.isEmpty(req.getCompletionTimeEnd())) {
            lambdaQuery.le(PaymentOrder::getCompletionTime, req.getCompletionTimeEnd());
            queryWrapper.le(PaymentOrder::getCompletionTime, req.getCompletionTimeEnd());
        }

        //--动态查询 完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            lambdaQuery.ge(PaymentOrder::getCompleteDuration, req.getCompleteDurationStart());
            queryWrapper.ge(PaymentOrder::getCompleteDuration, req.getCompleteDurationStart());
        }

        //--动态查询 完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            lambdaQuery.le(PaymentOrder::getCompleteDuration, req.getCompleteDurationEnd());
            queryWrapper.le(PaymentOrder::getCompleteDuration, req.getCompleteDurationEnd());
        }

        //--动态查询 匹配时长开始
//        if (req.getMatchDurationStart()!=null) {
//            lambdaQuery.ge(PaymentOrder::getMatchDuration, req.getMatchDurationStart());
//            queryWrapper.ge(PaymentOrder::getMatchDuration, req.getMatchDurationStart());
//        }

        //--动态查询 匹配时长结束
//        if (req.getMatchDurationEnd()!=null) {
//            lambdaQuery.le(PaymentOrder::getMatchDuration, req.getMatchDurationEnd());
//            queryWrapper.le(PaymentOrder::getMatchDuration, req.getMatchDurationEnd());
//        }

        //--动态查询 匹配时长结束
        if (StringUtils.isNotBlank(req.getMatchOrder())) {
            lambdaQuery.eq(PaymentOrder::getMatchOrder, req.getMatchOrder());
            queryWrapper.eq(PaymentOrder::getMatchOrder, req.getMatchOrder());
        }

        // 查询风控标识
        if (!ObjectUtils.isEmpty(req.getRiskTag())) {
            String timeOutCode = RiskTagEnum.ORDER_TIME_OUT.getCode();
            String blackIpCode = RiskTagEnum.BLACK_IP.getCode();
            String normalCode = RiskTagEnum.Normal.getCode();
            if(req.getRiskTag().equals(timeOutCode)){
                lambdaQuery.eq(PaymentOrder::getRiskTagTimeout, 1);
                queryWrapper.eq(PaymentOrder::getRiskTagTimeout, 1);
            }
            else if(req.getRiskTag().equals(blackIpCode)){
                lambdaQuery.eq(PaymentOrder::getRiskTagBlack, 1);
                queryWrapper.eq(PaymentOrder::getRiskTagBlack, 1);
            }
            else if(req.getRiskTag().equals(normalCode)){
                lambdaQuery.eq(PaymentOrder::getRiskTagTimeout, 0);
                queryWrapper.eq(PaymentOrder::getRiskTagTimeout, 0);
                lambdaQuery.eq(PaymentOrder::getRiskTagBlack, 0);
                queryWrapper.eq(PaymentOrder::getRiskTagBlack, 0);
            }
            else{
                lambdaQuery.eq(PaymentOrder::getId, -1);
                queryWrapper.eq(PaymentOrder::getId, -1);
            }
        }

        if (ObjectUtils.isNotEmpty(req.getKycAutoCompletionStatus())) {
            lambdaQuery.le(PaymentOrder::getKycAutoCompletionStatus, req.getKycAutoCompletionStatus());
            queryWrapper.le(PaymentOrder::getKycAutoCompletionStatus, req.getKycAutoCompletionStatus());
        }
        // 支付类型
        if (req.getPayType() != null) {
            if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_CARD.getCode())){
                lambdaQuery.and(w -> w.or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
                queryWrapper.and(w -> w.or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
            }else if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_UPI.getCode())){
                // 支持upi INDIAN_UPI, INDIAN_CARD_UPI_FIX
                lambdaQuery.and(w -> w.or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
                queryWrapper.and(w -> w.or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(PaymentOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
            }else{
                lambdaQuery.eq(PaymentOrder::getPayType, req.getPayType());
                queryWrapper.eq(PaymentOrder::getPayType, req.getPayType());
            }
        }

        Page<PaymentOrder> finalPage = page;
        CompletableFuture<PaymentOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<PaymentOrder>> paymentOrderFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, paymentOrderFuture);

        page = paymentOrderFuture.get();
        PaymentOrder totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal());
        extent.put("actualAmountTotal", totalInfo.getActualAmountTotal());
        extent.put("bonusTotal", totalInfo.getBonusTotal());
        extent.put("ITokenTotal", totalInfo.getITokenTotal());

        List<PaymentOrder> records = page.getRecords();

        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal actualAmountPageTotal = BigDecimal.ZERO;
        BigDecimal bonusPageTotal = BigDecimal.ZERO;
        BigDecimal iTokenPageTotal = BigDecimal.ZERO;

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<PaymentOrderListPageDTO> paymentOrderListVos = new ArrayList<>();
        for (PaymentOrder record : records) {
            List<String> riskTag = new ArrayList<>();
            amountPageTotal = amountPageTotal.add(record.getAmount());
            actualAmountPageTotal = actualAmountPageTotal.add(record.getActualAmount());
            bonusPageTotal = bonusPageTotal.add(record.getBonus());
            iTokenPageTotal = iTokenPageTotal.add(record.getItokenNumber());

            PaymentOrderListPageDTO paymentOrderListPageDTO = new PaymentOrderListPageDTO();
            BeanUtil.copyProperties(record, paymentOrderListPageDTO);
            if(!ObjectUtils.isEmpty(record.getRiskTagTimeout()) && record.getRiskTagTimeout() != 0){
                riskTag.add(RiskTagEnum.ORDER_TIME_OUT.getCode());
            }
            if(!ObjectUtils.isEmpty(record.getRiskTagBlack()) && record.getRiskTagBlack() != 0){
                riskTag.add(RiskTagEnum.BLACK_IP.getCode());
            }
            if(riskTag.isEmpty()){
                riskTag.add(RiskTagEnum.Normal.getCode());
            }
            paymentOrderListPageDTO.setRiskTag(String.join(",", riskTag));
            paymentOrderListVos.add(paymentOrderListPageDTO);
        }
        extent.put("amountPageTotal", amountPageTotal);
        extent.put("actualPageAmountTotal", actualAmountPageTotal);
        extent.put("bonusPageTotal", bonusPageTotal);
        extent.put("ITokenPageTotal", iTokenPageTotal);
        return PageUtils.flush(page, paymentOrderListVos, extent);
    }

    @Override
    public PageReturn<PaymentOrderExportDTO> listPageExport(PaymentOrderListPageReq req) {
        PageReturn<PaymentOrderListPageDTO> paymentOrderPageReturn = listPage(req);
        List<PaymentOrderExportDTO> resultList = new ArrayList<>();
        for (PaymentOrderListPageDTO paymentOrderListPageDTO : paymentOrderPageReturn.getList()) {
            PaymentOrderExportDTO paymentOrderExportDTO = new PaymentOrderExportDTO();
            BeanUtils.copyProperties(paymentOrderListPageDTO, paymentOrderExportDTO);
            String nameByCode = OrderStatusEnum.getNameByCode(paymentOrderListPageDTO.getOrderStatus());
            paymentOrderExportDTO.setOrderStatus(nameByCode);
            if(paymentOrderListPageDTO.getAmount() != null){
                paymentOrderExportDTO.setAmount(paymentOrderListPageDTO.getAmount().toString());
            }
            if(paymentOrderListPageDTO.getActualAmount() != null){
                paymentOrderExportDTO.setActualAmount(paymentOrderListPageDTO.getActualAmount().toString());
            }
            if(paymentOrderListPageDTO.getBonus() != null){
                paymentOrderExportDTO.setBonus(paymentOrderListPageDTO.getBonus().toString());
            }
            String tradeCallBackStatus = NotifyStatusEnum.getNameByCode(paymentOrderListPageDTO.getTradeCallbackStatus());
            paymentOrderExportDTO.setTradeCallbackStatus(tradeCallBackStatus);
            String completeDurationStr = "";
            if(paymentOrderListPageDTO.getCompleteDuration() != null && Integer.parseInt(paymentOrderListPageDTO.getCompleteDuration()) != 0){
                completeDurationStr = DurationCalculatorUtil.getOrderCompleteDuration(paymentOrderListPageDTO.getCompleteDuration());
            }
            paymentOrderExportDTO.setCompleteDuration(completeDurationStr);
            resultList.add(paymentOrderExportDTO);
        }
        Page<PaymentOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(paymentOrderPageReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }


    @Override
    public PageReturn<PaymentOrderListPageDTO> listRecordPage(PaymentOrderListPageReq req) {

        Page<PaymentOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<PaymentOrder> lambdaQuery = lambdaQuery();
        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(PaymentOrder::getMemberId, req.getMemberId());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMerchantName())) {
            lambdaQuery.eq(PaymentOrder::getMerchantName, req.getMerchantName());
        }

        //--动态查询 商户订单号
        if (!StringUtils.isEmpty(req.getMerchantOrder())) {
            lambdaQuery.eq(PaymentOrder::getMerchantOrder, req.getMerchantOrder());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(PaymentOrder::getMemberId, req.getMemberId());
        }

        //--动态查询 平台订单号
        if (!StringUtils.isEmpty(req.getPlatformOrder())) {
            lambdaQuery.eq(PaymentOrder::getPlatformOrder, req.getPlatformOrder());
        }

        //--动态查询 支付状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            lambdaQuery.eq(PaymentOrder::getOrderStatus, req.getOrderStatus());
        }

        //--动态查询 回调状态
//        if (!StringUtils.isEmpty(req.getTradeCallbackStatus())) {
//            lambdaQuery.eq(PaymentOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
//        }


        //--动态查询 提现时间开始
        if (req.getCreateTimeStart() != null) {
            lambdaQuery.ge(PaymentOrder::getCreateTime, req.getCreateTimeStart());
        }

        //--动态查询 提现结束
        if (req.getCreateTimeEnd() != null) {
            lambdaQuery.le(PaymentOrder::getCreateTime, req.getCreateTimeEnd());
        }

        //--动态查询 提现时间开始
        if (req.getCompletionTimeStart() != null) {
            lambdaQuery.ge(PaymentOrder::getCompletionTime, req.getCompletionTimeStart());
        }

        //--动态查询 提现结束
        if (req.getCompletionTimeEnd() != null) {
            lambdaQuery.le(PaymentOrder::getCompletionTime, req.getCompletionTimeEnd());
        }

        //--动态查询 完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            lambdaQuery.ge(PaymentOrder::getCompleteDuration, req.getCompleteDurationStart());
        }

        //--动态查询 完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            lambdaQuery.le(PaymentOrder::getCompleteDuration, req.getCompleteDurationEnd());
        }

        //--动态查询 匹配时长开始
//        if (req.getMatchDurationStart()!=null) {
//            lambdaQuery.ge(PaymentOrder::getMatchDuration, req.getMatchDurationStart());
//        }

        //--动态查询 匹配时长结束
//        if (req.getMatchDurationEnd()!=null) {
//            lambdaQuery.le(PaymentOrder::getMatchDuration, req.getMatchDurationEnd());
//        }
        List<String> list = new ArrayList<String>();
        String[] arr = "7,8,9,10,12".split(",");


        lambdaQuery.in(PaymentOrder::getOrderStatus, Arrays.asList(arr));


        lambdaQuery.orderByDesc(PaymentOrder::getId);


        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<PaymentOrder> records = page.getRecords();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<PaymentOrderListPageDTO> paymentOrderListVos = new ArrayList<>();
        for (PaymentOrder record : records) {
            PaymentOrderListPageDTO paymentOrderListPageDTO = new PaymentOrderListPageDTO();
            BeanUtil.copyProperties(record, paymentOrderListPageDTO);

            paymentOrderListVos.add(paymentOrderListPageDTO);
        }

        return PageUtils.flush(page, paymentOrderListVos);
    }


    @Override
    public PaymentOrderListPageDTO listRecordTotalPage(PaymentOrderListPageReq req) {

        QueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "sum(amount) as amount"

        );

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMemberId())) {
            queryWrapper.eq("member_id", req.getMemberId());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMerchantName())) {
            queryWrapper.eq("merchant_name", req.getMerchantName());
        }

        //--动态查询 商户订单号
        if (!StringUtils.isEmpty(req.getMerchantOrder())) {
            queryWrapper.eq("merchant_order", req.getMerchantOrder());
        }

        //--动态查询 平台订单号
        if (!StringUtils.isEmpty(req.getPlatformOrder())) {
            queryWrapper.eq("platform_order", req.getPlatformOrder());
        }

        //--动态查询 支付状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            queryWrapper.eq("order_status", req.getOrderStatus());
        }

        //--动态查询 回调状态
        if (!StringUtils.isEmpty(req.getTradeCallbackStatus())) {
            queryWrapper.eq("trade_callback_status", req.getTradeCallbackStatus());
        }


        //--动态查询 提现时间开始
        if (req.getCreateTimeStart() != null) {
            queryWrapper.ge("create_time", req.getCreateTimeStart());
        }

        //--动态查询 提现时间结束
        if (req.getCreateTimeEnd() != null) {
            queryWrapper.le("create_time", req.getCreateTimeEnd());
        }

        //--动态查询 提现时间开始
        if (req.getCompletionTimeStart() != null) {
            queryWrapper.ge("completion_time", req.getCompletionTimeStart());
        }

        //--动态查询 提现结束
        if (req.getCompletionTimeEnd() != null) {
            queryWrapper.le("completion_time", req.getCompletionTimeEnd());
        }

        //--动态查询 完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            queryWrapper.ge("complete_duration", req.getCompleteDurationStart());
        }

        //--动态查询 完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            queryWrapper.le("complete_duration", req.getCompleteDurationEnd());
        }

        //--动态查询 匹配时长开始
        if (req.getMatchDurationStart()!=null) {
            queryWrapper.ge("match_duration", req.getMatchDurationStart());
        }

        //--动态查询 匹配时长结束
        if (req.getMatchDurationEnd()!=null) {
            queryWrapper.le("match_duration", req.getMatchDurationEnd());
        }
        String[] arr = "7,8,9,10,12".split(",");
        queryWrapper.in("order_status", Arrays.asList(arr));


        queryWrapper.orderByDesc("id");


        // 倒序排序
        //queryWrapper.orderByDesc(CollectionOrder::getId);
        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page, queryWrapper);
        List<Map<String, Object>> records = page.getRecords();
        if (records == null) return new PaymentOrderListPageDTO();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<PaymentOrderListPageDTO> list = jsonArray.toJavaList(PaymentOrderListPageDTO.class);
        PaymentOrderListPageDTO paymentOrderListPageDTO = list.get(0);

        return paymentOrderListPageDTO;
    }


    @Override
    public RestResult getPaymentOrderInfoByOrderNo(String merchantOrder) {
        //根据订单号查询代付订单详情
        QueryWrapper<PaymentOrder> paymentOrderQueryWrapper = new QueryWrapper<>();
        paymentOrderQueryWrapper.select("settlement_amount", "cost", "currentcy", "create_time",
                "callback_time", "order_status", "pay_type", "account_name", "account_number", "third_code", "merchant_code").eq("merchant_order", merchantOrder);
        PaymentOrder paymentOrder = getOne(paymentOrderQueryWrapper);

        System.out.println("paymentOrder: " + paymentOrder);

        if (paymentOrder != null) {
            PaymentOrderInfoVo paymentOrderInfoVo = new PaymentOrderInfoVo();
            BeanUtil.copyProperties(paymentOrder, paymentOrderInfoVo);
            //订单金额
            paymentOrderInfoVo.setAmount(paymentOrder.getAmount());

            //通过商户号查询商户名称
            QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
            merchantInfoQueryWrapper.select("username").eq("code", paymentOrder.getMerchantCode());
            MerchantInfo merchantInfo = merchantInfoService.getOne(merchantInfoQueryWrapper);
            if (merchantInfo != null) {
                paymentOrderInfoVo.setUsername(merchantInfo.getUsername());
            }
            System.out.println("paymentOrderInfoVo: " + paymentOrderInfoVo);


            return RestResult.ok(paymentOrderInfoVo);
        } else {
            return RestResult.failed("该笔订单不存在");
        }


    }


    /**
     * 根据订单号获取订单信息
     *
     * @param orderNo
     * @return {@link PaymentOrder}
     */
    @Override
    public PaymentOrder getPaymentOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(PaymentOrder::getPlatformOrder, orderNo).or().eq(PaymentOrder::getMerchantOrder, orderNo).one();
    }

    /**
     * 查询卖出订单列表
     *
     * @param req
     * @return {@link List}<{@link SellOrderListVo}>
     */
    @Override
    public List<SellOrderListVo> sellOrderList(SellOrderListReq req) {

        if (req == null) {
            req = new SellOrderListReq();
        }

        LambdaQueryChainWrapper<PaymentOrder> lambdaQuery = lambdaQuery();

        //--动态查询 订单状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            lambdaQuery.eq(PaymentOrder::getOrderStatus, req.getOrderStatus());
        }

        //--动态查询 时间 某天
        if (StringUtils.isNotEmpty(req.getDate())){
            LocalDate localDate = LocalDate.parse(req.getDate());
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);

            lambdaQuery.ge(PaymentOrder::getCreateTime, startOfDay);
            lambdaQuery.le(PaymentOrder::getCreateTime, endOfDay);
        }

        // 倒序排序
        lambdaQuery.orderByDesc(PaymentOrder::getId);

        List<PaymentOrder> paymentOrderList = lambdaQuery.list();

        ArrayList<SellOrderListVo> sellOrderListVoList = new ArrayList<>();

        for (PaymentOrder paymentOrder : paymentOrderList) {
            SellOrderListVo sellOrderListVo = new SellOrderListVo();
            BeanUtil.copyProperties(paymentOrder, sellOrderListVo);
            sellOrderListVoList.add(sellOrderListVo);
        }
        return sellOrderListVoList;
    }

    /**
     * 查看卖出订单详情
     *
     * @param platformOrder
     * @return {@link ViewSellOrderDetailsVo}
     */
    @Override
    public ViewSellOrderDetailsVo viewSellOrderDetails(String platformOrder) {
        PaymentOrder paymentOrder = lambdaQuery().eq(PaymentOrder::getPlatformOrder, platformOrder).one();
        ViewSellOrderDetailsVo viewSellOrderDetailsVo = new ViewSellOrderDetailsVo();
        BeanUtil.copyProperties(paymentOrder, viewSellOrderDetailsVo);
        return viewSellOrderDetailsVo;
    }

    /**
     * 根据收款id获取正在匹配中的订单
     *
     * @param collectionInfoId
     * @return {@link Integer}
     */
//    @Override
//    public Integer getMatchingOrdersBycollectionId(Long collectionInfoId) {
//        return lambdaQuery().eq(PaymentOrder::getCollectionInfoId, collectionInfoId).eq(PaymentOrder::getOrderStatus, OrderStatusEnum.BE_MATCHED.getCode()).count().intValue();
//    }

    /**
     * 获取卖出订单列表
     *
     * @param req
     * @param memberInfo
     * @return {@link List}<{@link SellOrderListVo}>
     */
    @Override
    public List<SellOrderListVo> getPaymentOrderOrderList(SellOrderListReq req, MemberInfo memberInfo) {

        if (req == null) {
            req = new SellOrderListReq();
        }

        LambdaQueryChainWrapper<PaymentOrder> lambdaQuery = lambdaQuery();

        //查询当前会员的卖出订单
        lambdaQuery.eq(PaymentOrder::getMemberId, memberInfo.getId());

        //--动态查询 订单状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            //对手动完成和已完成做兼容处理
            if (OrderStatusEnum.SUCCESS.getCode().equals(req.getOrderStatus())){
                lambdaQuery.nested(i -> i.eq(PaymentOrder::getOrderStatus, OrderStatusEnum.SUCCESS.getCode()));
            }else{
                lambdaQuery.eq(PaymentOrder::getOrderStatus, req.getOrderStatus());
            }
        }

        //--动态查询 时间 某天
        if (StringUtils.isNotEmpty(req.getDate())){
            LocalDate localDate = LocalDate.parse(req.getDate());
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);

            lambdaQuery.ge(PaymentOrder::getCreateTime, startOfDay);
            lambdaQuery.le(PaymentOrder::getCreateTime, endOfDay);
        }

        // 倒序排序
        lambdaQuery.orderByDesc(PaymentOrder::getId);

        List<PaymentOrder> list = lambdaQuery.list();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<SellOrderListVo> sellOrderListVoList = new ArrayList<>();
        for (PaymentOrder paymentOrder : list) {
            SellOrderListVo sellOrderListVo = new SellOrderListVo();
            BeanUtil.copyProperties(paymentOrder, sellOrderListVo);

            //设置支付方式
            sellOrderListVo.setPayType(paymentOrder.getPayType());

            //匹配剩余时间
            sellOrderListVo.setMatchExpireTime(redisUtil.getMatchRemainingTime(sellOrderListVo.getPlatformOrder()));
            //确认中剩余时间
            sellOrderListVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(sellOrderListVo.getPlatformOrder()));
            //待支付剩余时间
            sellOrderListVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(sellOrderListVo.getPlatformOrder()));

            //设置是否经过申诉
            if (paymentOrder.getAppealTime() != null){
                sellOrderListVo.setIsAppealed(1);
            }

            //优化超时显示
            //判断如果订单是支付中状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
            if (sellOrderListVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (sellOrderListVo.getPaymentExpireTime() == null || sellOrderListVo.getPaymentExpireTime() < 1)){
                sellOrderListVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
            }

            sellOrderListVoList.add(sellOrderListVo);
        }

        log.info("获取卖出订单列表: {}, req: {}, 会员账号: {}, 返回数据: {}", sellOrderListVoList, req, memberInfo.getMemberAccount(), list);

        return sellOrderListVoList;
    }

    /**
     * 根据匹配订单号获取卖出订单列表
     *
     * @param matchOrder
     * @return {@link List}<{@link SellOrderListVo}>
     */
    @Override
    public List<SellOrderListVo> getPaymentOrderListByMatchOrder(String matchOrder) {

        List<PaymentOrder> paymentOrderList = lambdaQuery().eq(PaymentOrder::getMatchOrder, matchOrder).orderByDesc(PaymentOrder::getId).list();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<SellOrderListVo> sellOrderListVoList = new ArrayList<>();
        for (PaymentOrder paymentOrder : paymentOrderList) {
            SellOrderListVo sellOrderListVo = new SellOrderListVo();
            BeanUtil.copyProperties(paymentOrder, sellOrderListVo);

            //匹配剩余时间
            sellOrderListVo.setMatchExpireTime(redisUtil.getMatchRemainingTime(paymentOrder.getPlatformOrder()));
            //确认中剩余时间
            sellOrderListVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(paymentOrder.getPlatformOrder()));
            //待支付剩余时间
            sellOrderListVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(paymentOrder.getPlatformOrder()));


            //优化剩余时间为0 状态还没更新的延迟
            //判断如果订单是待支付状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
            if (sellOrderListVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (sellOrderListVo.getPaymentExpireTime() == null || sellOrderListVo.getPaymentExpireTime() < 1)){
                sellOrderListVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
            }

            //如果是手动完成状态, 改为已完成状态
//            if (sellOrderListVo.getOrderStatus().equals(OrderStatusEnum.MANUAL_COMPLETION.getCode())){
//                sellOrderListVo.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
//            }

            sellOrderListVoList.add(sellOrderListVo);
        }

        return sellOrderListVoList;
    }

    @Override
    public List<PaymentOrder> getPaymentOrderByByMatchOrder(String matchOrder) {
        return lambdaQuery().eq(PaymentOrder::getMatchOrder, matchOrder).list();
    }

    @Override
    public Boolean manualCallback(PaymentOrderIdReq req) throws Exception {
        boolean result = false;
        try{
            PaymentOrder paymentOrder = this.baseMapper.selectById(req.getId());

            if(ObjectUtils.isEmpty(paymentOrder)){
                throw new BizException(ResultCode.ORDER_NOT_EXIST);
            }
            // 判断交易回调状态
            if(paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.SUCCESS.getCode()) || paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.MANUAL_SUCCESS.getCode())){
                throw new BizException(ResultCode.ORDER_ALREADY_CALLBACK);
            }else {
                // 回调
                result = asyncNotifyService.sendRechargeSuccessCallbackWithRecordRequest(paymentOrder.getMerchantOrder(), "2");
            }
        }catch (Exception e){
            return result;
        }

        return result;
    }


    /**
     * 查看该母订单是否已结束(查看母订单状态和该母订单下的子订单是否有未结束的订单)
     *
     * @param matchOrder
     * @return {@link Boolean}
     */
    @Override
    public Boolean existsActiveSubOrders(String matchOrder) {

        //查询母订单信息
        MatchPool matchPoolOrder = matchPoolService.getMatchPoolOrderByOrderNo(matchOrder);

        if (matchPoolOrder == null){
            return true;
        }

        //1 待匹配
        //2 匹配超时
        //14 进行中
        //查看母订单状态如果是还未结束 那么不进行操作
        Set<String> targetStatuses = new HashSet<>(Arrays.asList("1", "2", "14"));
        if (targetStatuses.contains(matchPoolOrder.getOrderStatus())) {
            return true;
        }


        // 定义"进行中" 和 交易成功的订单状态
        //1 待匹配
        //2 匹配超时
        //3 待支付
        //4 确认中
        //5 确认超时
        //6 申诉中
        //7 已完成
        //11 金额错误
        //14 进行中
        //15 手动完成
        //如果子订单有交易成功或进行中的 不进行操作
        String[] activeStatuses = new String[] {"1", "2", "3", "4", "5", "6", "7", "11", "14", "15"};

        Integer count = lambdaQuery()
                .eq(PaymentOrder::getMatchOrder, matchOrder)
                .in(PaymentOrder::getOrderStatus, (Object[]) activeStatuses)
                .count();

        return count > 0;
    }

    @Override
    public MemberOrderOverviewDTO getUsdtData(CommonDateLimitReq req) {
        return paymentOrderMapper.getMemberUsdtInfo(req.getStartTime(), req.getEndTime());
    }

    /**
     * 根据IP获取卖出订单
     *
     * @param ip
     * @return
     */
    @Override
    public List<PaymentOrder> getPaymentOrderByByIp(String ip) {
        LocalDate localDate = LocalDate.now().minusMonths(6);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        return lambdaQuery().eq(PaymentOrder::getClientIp, ip).ge(PaymentOrder::getCreateTime, startOfDay)
                .select(PaymentOrder::getMemberId, PaymentOrder::getPlatformOrder)
                .list();
    }

    /**
     * 标记订单为指定的tag
     *
     * @param riskTag
     * @param platformOrders
     */
    @Override
    @Transactional
    public void taggingOrders(String riskTag, List<String> platformOrders) {
        if (RiskTagEnum.getNameByCode(riskTag) == null) {
            return;
        }
        if (CollectionUtils.isEmpty(platformOrders)) {
            return;
        }
        LambdaUpdateChainWrapper<PaymentOrder> updateWrapper = lambdaUpdate().in(PaymentOrder::getPlatformOrder, platformOrders);
        if (RiskTagEnum.BLACK_IP.getCode().equals(riskTag)) {
            updateWrapper.set(PaymentOrder::getRiskTagBlack, 1);
        }else if (RiskTagEnum.ORDER_TIME_OUT.getCode().equals(riskTag)) {
            updateWrapper.set(PaymentOrder::getRiskTagTimeout, 1);
        }else if(RiskTagEnum.Normal.getCode().equals(riskTag)){
            updateWrapper.set(PaymentOrder::getRiskTagBlack, 0);
        }else {
            return;
        }
        updateWrapper.update();

    }

    @Override
    public RestResult<PaymentOrderInfoDTO> getInfoById(Long id) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setId(id);
        paymentOrder = getById(paymentOrder);
        PaymentOrderInfoDTO paymentOrderInfoDTO = new PaymentOrderInfoDTO();
        // 查询撮合信息
        if(ObjectUtils.isNotEmpty(paymentOrder.getPlatformOrder())){
            MatchingOrder matchingOrder = matchingOrderMapper.selectMatchingOrderByWithdrawOrder(paymentOrder.getPlatformOrder());
            if(ObjectUtils.isNotEmpty(matchingOrder)){
                // 根据撮合信息查询买入订单信息
                CollectionOrder orderByOrderNo = collectionOrderMapper.getOrderByOrderNo(matchingOrder.getCollectionPlatformOrder());
                // 获取随机码
                String randomCode = orderByOrderNo.getRandomCode();
                paymentOrderInfoDTO.setRandomCode(randomCode);
            }
        }
        BeanUtils.copyProperties(paymentOrder,paymentOrderInfoDTO);
        Long collectionInfoId = paymentOrder.getBankCollectionInfoId();
        // 获取拆单信息
        if(collectionInfoId == null && ObjectUtils.isNotEmpty(paymentOrder.getMatchOrder())){
            String matchOrder = paymentOrder.getMatchOrder();
            MatchPool matchPoolOrderByOrderNo = matchPoolService.getMatchPoolOrderByOrderNo(matchOrder);
            // 获取收款信息
            collectionInfoId = matchPoolOrderByOrderNo.getBankCollectionInfoId();
        }
        if(ObjectUtils.isNotEmpty(collectionInfoId)){
            CollectionInfo collectionInfo = collectionInfoMapper.selectCollectionInfoForUpdate(collectionInfoId);
            if(ObjectUtils.isNotEmpty(collectionInfo)){
                paymentOrderInfoDTO.setBankName(collectionInfo.getBankName());
                paymentOrderInfoDTO.setBankCardNumber(collectionInfo.getBankCardNumber());
                paymentOrderInfoDTO.setBankCardOwner(collectionInfo.getBankCardOwner());
                paymentOrderInfoDTO.setIfscCode(collectionInfo.getIfscCode());
            }
        }
        return RestResult.ok(paymentOrderInfoDTO);
    }

    @Override
    public CallBackDetailDTO callBackDetail(PaymentOrderIdReq req) {
        PaymentOrder one = lambdaQuery().eq(PaymentOrder::getId, req.getId()).one();
        CallBackDetailDTO dto = new CallBackDetailDTO();
        BeanUtil.copyProperties(one, dto);
        return dto;
    }

}
