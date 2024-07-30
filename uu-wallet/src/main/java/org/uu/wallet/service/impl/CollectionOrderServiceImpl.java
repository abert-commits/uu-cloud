package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.uu.common.mybatis.util.SpringContextUtil;
import org.uu.common.pay.dto.CallBackDetailDTO;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.dto.CollectionOrderExportDTO;
import org.uu.common.pay.req.CollectionOrderIdReq;
import org.uu.common.pay.req.CollectionOrderListPageReq;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.KycPartnersMapper;
import org.uu.wallet.mapper.MatchingOrderMapper;
import org.uu.wallet.req.BuyOrderListReq;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.*;
import org.uu.wallet.vo.*;
import org.jetbrains.annotations.NotNull;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CollectionOrderServiceImpl extends ServiceImpl<CollectionOrderMapper, CollectionOrder> implements ICollectionOrderService {

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private IPaymentOrderService paymentOrderService;

    @Autowired
    private IMatchingOrderService matchingOrderService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MatchingOrderMapper matchingOrderMapper;


    @Autowired
    private AmountChangeUtil amountChangeUtil;
    //private final ISellService sellService;

    @Autowired
    private IMemberInfoService memberInfoService;
    @Autowired
    private ITradeConfigService tradeConfigService;
    @Autowired
    private ICollectionInfoService collectionInfoService;
    @Autowired
    private IAppealOrderService appealOrderService;
    @Resource
    AsyncNotifyService asyncNotifyService;
    private final KycPartnersMapper kycPartnersMapper;

    /**
     * 查询买入订单列表
     *
     * @param req
     * @return {@link RestResult}<{@link List}<{@link BuyOrderListVo}>>
     */
    @Override
    public RestResult<PageReturn<BuyOrderListVo>> buyOrderList(BuyOrderListReq req) {

        if (req == null) {
            req = new BuyOrderListReq();
        }

        Page<CollectionOrder> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(req.getPageNo());
        pageCollectionOrder.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery = lambdaQuery();

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null){
            log.error("查询买入订单列表失败: 获取会员信息失败: {}", memberInfo);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询当前会员的买入订单
        lambdaQuery.eq(CollectionOrder::getMemberId, memberInfo.getId());

        //--动态查询 订单状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {

            //对手动完成和已完成做兼容处理
            if (OrderStatusEnum.SUCCESS.getCode().equals(req.getOrderStatus())){
                lambdaQuery.nested(i -> i.eq(CollectionOrder::getOrderStatus, OrderStatusEnum.SUCCESS.getCode()));
            }else{
                lambdaQuery.eq(CollectionOrder::getOrderStatus, req.getOrderStatus());
            }
        }


        //--动态查询 时间 某天
        if (StringUtils.isNotEmpty(req.getDate())){
            LocalDate localDate = LocalDate.parse(req.getDate());
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);

            lambdaQuery.ge(CollectionOrder::getCreateTime, startOfDay);
            lambdaQuery.le(CollectionOrder::getCreateTime, endOfDay);
        }

        // 倒序排序
        lambdaQuery.orderByDesc(CollectionOrder::getId);

        baseMapper.selectPage(pageCollectionOrder, lambdaQuery.getWrapper());

        List<CollectionOrder> records = pageCollectionOrder.getRecords();

        PageReturn<CollectionOrder> flush = PageUtils.flush(pageCollectionOrder, records);

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<BuyOrderListVo> buyOrderListVoList = new ArrayList<>();

        for (CollectionOrder collectionOrder : flush.getList()) {

            BuyOrderListVo buyOrderListVo = new BuyOrderListVo();
            BeanUtil.copyProperties(collectionOrder, buyOrderListVo);

            //设置待支付剩余时间
            buyOrderListVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(buyOrderListVo.getPlatformOrder()));

            //设置确认中 剩余时间
            buyOrderListVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(buyOrderListVo.getPlatformOrder()));

            //设置是否经过申诉
            if (collectionOrder.getAppealTime() != null){
                buyOrderListVo.setIsAppealed(1);
            }

            //判断如果订单是支付中状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
            if (buyOrderListVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (buyOrderListVo.getPaymentExpireTime() == null || buyOrderListVo.getPaymentExpireTime() < 1)){
                buyOrderListVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
            }

//            //如果是手动完成状态, 改为已完成状态
//            if (buyOrderListVo.getOrderStatus().equals(OrderStatusEnum.MANUAL_COMPLETION.getCode())){
//                buyOrderListVo.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
//            }

            buyOrderListVoList.add(buyOrderListVo);
        }

        PageReturn<BuyOrderListVo> buyOrderListVoPageReturn = new PageReturn<>();
        buyOrderListVoPageReturn.setPageNo(flush.getPageNo());
        buyOrderListVoPageReturn.setPageSize(flush.getPageSize());
        buyOrderListVoPageReturn.setTotal(flush.getTotal());
        buyOrderListVoPageReturn.setList(buyOrderListVoList);

        log.info("获取买入订单列表成功: 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), req, buyOrderListVoPageReturn);

        return RestResult.ok(buyOrderListVoPageReturn);
    }

    /**
     * 查看买入订单详情
     *
     * @param platformOrder
     * @return {@link ViewBuyOrderDetailsVo}
     */
    @Override
    public ViewBuyOrderDetailsVo viewBuyOrderDetails(String platformOrder) {

        CollectionOrder collectionOrder = lambdaQuery().eq(CollectionOrder::getPlatformOrder, platformOrder).one();
        ViewBuyOrderDetailsVo viewBuyOrderDetailsVo = new ViewBuyOrderDetailsVo();
        BeanUtil.copyProperties(collectionOrder, viewBuyOrderDetailsVo);
        return viewBuyOrderDetailsVo;
    }

    @Override
    public PageReturn<CollectionOrderDTO> listRecordPage(CollectionOrderListPageReq req) {

        Page<CollectionOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery = lambdaQuery();

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMerchantCode())) {
            lambdaQuery.eq(CollectionOrder::getMerchantCode, req.getMerchantCode());
        }

        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(CollectionOrder::getMemberId, req.getMemberId());
        }
        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getUtr())) {
            lambdaQuery.eq(CollectionOrder::getUtr, req.getUtr());
        }

        //--动态查询 商户订单号
        if (!StringUtils.isEmpty(req.getMerchantOrder())) {
            lambdaQuery.eq(CollectionOrder::getMerchantOrder, req.getMerchantOrder());
        }

        //--动态查询 平台订单号
        if (!StringUtils.isEmpty(req.getPlatformOrder())) {
            lambdaQuery.eq(CollectionOrder::getPlatformOrder, req.getPlatformOrder());
        }

        //--动态查询 支付状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            lambdaQuery.eq(CollectionOrder::getOrderStatus, req.getOrderStatus());
        }

        //--动态查询 回调状态
        if (!StringUtils.isEmpty(req.getTradeCallbackStatus())) {
            lambdaQuery.eq(CollectionOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
        }


        //--动态查询 完成时间开始
        if (req.getCompletionTimeStart() != null) {
            lambdaQuery.ge(CollectionOrder::getCompletionTime, req.getCompletionTimeStart());
        }

        //--动态查询 完成时间结束
        if (req.getCompletionTimeEnd() != null) {
            lambdaQuery.le(CollectionOrder::getCompletionTime, req.getCompletionTimeEnd());
        }

        //--完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            lambdaQuery.ge(CollectionOrder::getCompleteDuration, req.getCompleteDurationStart());
        }

        //--完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            lambdaQuery.le(CollectionOrder::getCompleteDuration, req.getCompleteDurationEnd());
        }

        // 倒序排序
        lambdaQuery.orderByDesc(CollectionOrder::getId);

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CollectionOrder> records = page.getRecords();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<CollectionOrderDTO> collectionOrderListVos = new ArrayList<>();
        for (CollectionOrder record : records) {
            CollectionOrderDTO collectionOrderListVo = new CollectionOrderDTO();
            BeanUtil.copyProperties(record, collectionOrderListVo);
            //CollectionOrderDTO.setPayType(PayTypeEnum.getNameByCode(collectionOrderListVo.getPayType()));
            collectionOrderListVos.add(collectionOrderListVo);
        }
//        IPage<CollectionOrderListVo> convert = page.convert(CollectionOrder -> BeanUtil.copyProperties(CollectionOrder, CollectionOrderListVo.class));
        return PageUtils.flush(page, collectionOrderListVos);
    }


    @Override
    @SneakyThrows
    public PageReturn<CollectionOrderDTO> listPage(CollectionOrderListPageReq req) {

        Page<CollectionOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());


        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery = lambdaQuery();

        // 新增统计金额字段总计字段
        LambdaQueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<CollectionOrder>()
                .select("IFNULL(sum(amount),0) as amountTotal," +
                        "IFNULL(sum(actual_amount), 0) as actualAmountTotal," +
                        "IFNULL(sum(bonus),0) as bonusTotal," +
                        "IFNULL(sum(itoken_number), 0) as ITokenTotal"
                )
                .lambda();

        // 排序
        if(ObjectUtils.isNotEmpty(req.getColumn())){
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }else{
            lambdaQuery.orderByDesc(CollectionOrder::getId);
        }
        if (ObjectUtils.isNotEmpty(req.getMemberType())) {
            lambdaQuery.eq(CollectionOrder::getMemberType, req.getMemberType());
            queryWrapper.eq(CollectionOrder::getMemberType, req.getMemberType());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMerchantCode())) {
            lambdaQuery.eq(CollectionOrder::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(CollectionOrder::getMerchantCode, req.getMerchantCode());
        }

        //--动态查询 币种
        if (!StringUtils.isEmpty(req.getCurrency())) {
            lambdaQuery.eq(CollectionOrder::getCurrency, req.getCurrency());
            queryWrapper.eq(CollectionOrder::getCurrency, req.getCurrency());
        }

        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.and(e -> e.or().eq(CollectionOrder::getMemberId, req.getMemberId()).or().eq(CollectionOrder::getMemberAccount, req.getMemberId()));
            queryWrapper.and(e -> e.or().eq(CollectionOrder::getMemberId, req.getMemberId()).or().eq(CollectionOrder::getMemberAccount, req.getMemberId()));
        }
        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getUtr())) {
            lambdaQuery.eq(CollectionOrder::getUtr, req.getUtr());
            queryWrapper.eq(CollectionOrder::getUtr, req.getUtr());
        }

        //--动态查询 商户订单号
        if (!StringUtils.isEmpty(req.getMerchantOrder())) {
            lambdaQuery.and(e -> e.or().eq(CollectionOrder::getMerchantOrder, req.getMerchantOrder()).or().eq(CollectionOrder::getMerchantPaymentOrder, req.getMerchantOrder()));
            queryWrapper.and(e -> e.or().eq(CollectionOrder::getMerchantOrder, req.getMerchantOrder()).or().eq(CollectionOrder::getMerchantPaymentOrder, req.getMerchantOrder()));
        }

        //--动态查询 平台订单号
        if (!StringUtils.isEmpty(req.getPlatformOrder())) {
            lambdaQuery.eq(CollectionOrder::getPlatformOrder, req.getPlatformOrder());
            queryWrapper.eq(CollectionOrder::getPlatformOrder, req.getPlatformOrder());
        }

        //--动态查询 支付状态
        if (!StringUtils.isEmpty(req.getOrderStatus())) {
            lambdaQuery.eq(CollectionOrder::getOrderStatus, req.getOrderStatus());
            queryWrapper.eq(CollectionOrder::getOrderStatus, req.getOrderStatus());
        }

        //--动态查询 回调状态
        if (!StringUtils.isEmpty(req.getTradeCallbackStatus())) {
            lambdaQuery.eq(CollectionOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
            queryWrapper.eq(CollectionOrder::getTradeCallbackStatus, req.getTradeCallbackStatus());
        }


        //--动态查询 完成时间开始
        if (req.getCompletionTimeStart() != null) {
            lambdaQuery.ge(CollectionOrder::getCompletionTime, req.getCompletionTimeStart());
            queryWrapper.ge(CollectionOrder::getCompletionTime, req.getCompletionTimeStart());
        }

        //--动态查询 完成时间结束
        if (req.getCompletionTimeEnd() != null) {
            lambdaQuery.le(CollectionOrder::getCompletionTime, req.getCompletionTimeEnd());
            queryWrapper.le(CollectionOrder::getCompletionTime, req.getCompletionTimeEnd());
        }

        //--动态查询 完成时间开始
        if (ObjectUtils.isNotEmpty(req.getStartTime())) {
            lambdaQuery.ge(CollectionOrder::getCreateTime, req.getStartTime());
            queryWrapper.ge(CollectionOrder::getCreateTime, req.getStartTime());
        }

        //--动态查询 完成时间结束
        if (ObjectUtils.isNotEmpty(req.getEndTime())) {
            lambdaQuery.le(CollectionOrder::getCreateTime, req.getEndTime());
            queryWrapper.le(CollectionOrder::getCreateTime, req.getEndTime());
        }

        //--完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            lambdaQuery.ge(CollectionOrder::getCompleteDuration, req.getCompleteDurationStart());
            queryWrapper.ge(CollectionOrder::getCompleteDuration, req.getCompleteDurationStart());
        }

        //--完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            lambdaQuery.le(CollectionOrder::getCompleteDuration, req.getCompleteDurationEnd());
            queryWrapper.le(CollectionOrder::getCompleteDuration, req.getCompleteDurationEnd());
        }
        // 查询风控标识
        if (!ObjectUtils.isEmpty(req.getRiskTag())) {
            String blackIpCode = RiskTagEnum.BLACK_IP.getCode();
            String normalCode = RiskTagEnum.Normal.getCode();
            if(req.getRiskTag().equals(blackIpCode)){
                lambdaQuery.eq(CollectionOrder::getRiskTagBlack, 1);
                queryWrapper.eq(CollectionOrder::getRiskTagBlack, 1);
            }
            else if(req.getRiskTag().equals(normalCode)){
                lambdaQuery.eq(CollectionOrder::getRiskTagBlack, 0);
                queryWrapper.eq(CollectionOrder::getRiskTagBlack, 0);
            }
            // 列表无余额过低筛选和操作超时
            else{
                lambdaQuery.eq(CollectionOrder::getId, -1);
                queryWrapper.eq(CollectionOrder::getId, -1);
            }
        }

        if (ObjectUtils.isNotEmpty(req.getKycAutoCompletionStatus())) {
            lambdaQuery.eq(CollectionOrder::getKycAutoCompletionStatus, req.getKycAutoCompletionStatus());
            queryWrapper.eq(CollectionOrder::getKycAutoCompletionStatus, req.getKycAutoCompletionStatus());
        }
        // 支付类型
        if (req.getPayType() != null) {
            if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_CARD.getCode())){
                lambdaQuery.and(w -> w.or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
                queryWrapper.and(w -> w.or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
            }else if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_UPI.getCode())){
                // 支持upi INDIAN_UPI, INDIAN_CARD_UPI_FIX
                lambdaQuery.and(w -> w.or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
                queryWrapper.and(w -> w.or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(CollectionOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
            }else{
                lambdaQuery.eq(CollectionOrder::getPayType, req.getPayType());
                queryWrapper.eq(CollectionOrder::getPayType, req.getPayType());
            }
        }


        Page<CollectionOrder> finalPage = page;
        CompletableFuture<CollectionOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<CollectionOrder>> collectionFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));

        CompletableFuture.allOf(totalFuture, collectionFuture);

        CollectionOrder totalInfo = totalFuture.get();

        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal());
        extent.put("actualAmountTotal", totalInfo.getActualAmountTotal());
        extent.put("bonusTotal", totalInfo.getBonusTotal());
        extent.put("ITokenTotal", totalInfo.getITokenTotal());

        page = collectionFuture.get();
        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal actualAmountPageTotal = BigDecimal.ZERO;
        BigDecimal bonusPageTotal = BigDecimal.ZERO;
        BigDecimal iTokenPageTotal = BigDecimal.ZERO;

        List<CollectionOrder> records = page.getRecords();
        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<CollectionOrderDTO> collectionOrderListVos = new ArrayList<>();
        for (CollectionOrder record : records) {
            List<String> riskTag = new ArrayList<>();
            amountPageTotal = amountPageTotal.add(record.getAmount());
            actualAmountPageTotal = actualAmountPageTotal.add(record.getActualAmount());
            bonusPageTotal = bonusPageTotal.add(record.getBonus());
            iTokenPageTotal = iTokenPageTotal.add(record.getItokenNumber());
            CollectionOrderDTO collectionOrderListVo = new CollectionOrderDTO();
            BeanUtil.copyProperties(record, collectionOrderListVo);
            if(!ObjectUtils.isEmpty(record.getRiskTagBlack()) && record.getRiskTagBlack() != 0){
                riskTag.add(RiskTagEnum.BLACK_IP.getCode());
            }
            if(riskTag.isEmpty()){
                riskTag.add(RiskTagEnum.Normal.getCode());
            }
            collectionOrderListVo.setRiskTag(String.join(",", riskTag));
            collectionOrderListVos.add(collectionOrderListVo);
        }

        extent.put("amountPageTotal", amountPageTotal);
        extent.put("actualPageAmountTotal", actualAmountPageTotal);
        extent.put("bonusPageTotal", bonusPageTotal);
        extent.put("ITokenPageTotal", iTokenPageTotal);
        return PageUtils.flush(page, collectionOrderListVos, extent);
    }

    @Override
    public PageReturn<CollectionOrderExportDTO> listPageExport(CollectionOrderListPageReq req) {
        PageReturn<CollectionOrderDTO> collectionOrderReturn = listPage(req);

        List<CollectionOrderExportDTO> resultList = new ArrayList<>();

        for (CollectionOrderDTO collectionOrderDTO : collectionOrderReturn.getList()) {
            CollectionOrderExportDTO collectionOrderExportDTO = new CollectionOrderExportDTO();
            BeanUtils.copyProperties(collectionOrderDTO, collectionOrderExportDTO);
            String nameByCode = OrderStatusEnum.getNameByCode(collectionOrderDTO.getOrderStatus());
            collectionOrderExportDTO.setOrderStatus(nameByCode);
            if(collectionOrderDTO.getActualAmount() != null){
                collectionOrderExportDTO.setActualAmount(collectionOrderDTO.getActualAmount().toString());
            }
            if(collectionOrderDTO.getAmount() != null){
                collectionOrderExportDTO.setAmount(collectionOrderDTO.getAmount().toString());
            }
            if(collectionOrderDTO.getCompleteDuration() != null){
                String orderCompleteDuration = DurationCalculatorUtil.getOrderCompleteDuration(collectionOrderDTO.getCompleteDuration().toString());
                collectionOrderExportDTO.setCompleteDuration(orderCompleteDuration);
            }
            String tradeCallBackStatus = NotifyStatusEnum.getNameByCode(collectionOrderDTO.getTradeCallbackStatus());
            collectionOrderExportDTO.setTradeCallbackStatus(tradeCallBackStatus);
            resultList.add(collectionOrderExportDTO);
        }
        Page<CollectionOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(collectionOrderReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }


    @Transactional
    @Override
    public CollectionOrderDTO pay(CollectionOrderIdReq req) {
        CollectionOrder collectionOrder = new CollectionOrder();
        collectionOrder.setId(req.getId());
        collectionOrder = this.baseMapper.selectById(collectionOrder);
        if(collectionOrder.getOrderStatus().equals(OrderStatusEnum.SUCCESS.getCode())){
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
        MatchingOrder matchingOrder = matchingOrderService.getMatchingOrderByCollection(collectionOrder.getPlatformOrder());
        if(!ObjectUtils.isEmpty(matchingOrder)){

            // 更新卖出订单状态
            matchingOrder.setStatus(OrderStatusEnum.SUCCESS.getCode());
            matchingOrderMapper.updateById(matchingOrder);

            collectionOrder.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
            collectionOrder.setActualAmount(req.getActualAmount());
            collectionOrder.setCompletionTime(LocalDateTime.now());
            collectionOrder.setUpdateBy(req.getCompletedBy());

            baseMapper.updateById(collectionOrder);

            //sellService.transactionSuccessHandler(matchingOrder.getCollectionPlatformOrder());

        }

        //amountChangeUtil.insertMemberChangeAmountRecord(collectionOrder.getMemberId(), req.getActualAmount(), ChangeModeEnum.ADD, "ARB", collectionOrder.getPlatformOrder(),  MemberAccountChangeEnum.RECHARGE, req.getCompletedBy());
        CollectionOrderDTO collectionOrderDTO = new CollectionOrderDTO();
        BeanUtil.copyProperties(collectionOrder, collectionOrderDTO);
        return collectionOrderDTO;

    }

    @Override
    public CollectionOrderDTO listPageRecordTotal(CollectionOrderListPageReq req) {

        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "sum(amount) as amount"

        );

        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getMemberId())) {
            queryWrapper.eq("member_id", req.getMemberId());
        }
        //--动态查询 会员ID
        if (!StringUtils.isEmpty(req.getUtr())) {
            queryWrapper.eq("utr", req.getUtr());
        }

        //--动态查询 商户号
        if (!StringUtils.isEmpty(req.getMerchantCode())) {
            queryWrapper.eq("merchant_code", req.getMerchantCode());
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


        //--动态查询 完成时间开始
        if (req.getCompletionTimeStart() != null) {
            queryWrapper.ge("completion_time", req.getCompletionTimeStart());
        }

        //--动态查询 完成时间结束
        if (req.getCompletionTimeEnd() != null) {
            queryWrapper.le("completion_time", req.getCompletionTimeEnd());
        }

        //--完成时长开始
        if (req.getCompleteDurationStart()!=null) {
            queryWrapper.ge("complete_duration", req.getCompleteDurationStart());
        }

        //--完成时长结束
        if (req.getCompleteDurationEnd()!=null) {
            queryWrapper.le("complete_duration", req.getCompleteDurationEnd());
        }


        // 倒序排序
        //queryWrapper.orderByDesc(CollectionOrder::getId);
        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page, queryWrapper);
        List<Map<String, Object>> records = page.getRecords();
        if (records == null) return new CollectionOrderDTO();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<CollectionOrderDTO> list = jsonArray.toJavaList(CollectionOrderDTO.class);
        CollectionOrderDTO collectionOrderDTO = list.get(0);

        return collectionOrderDTO;


    }

    @Override
    public Boolean manualCallback(CollectionOrderIdReq req) {
        boolean result = false;
        try{
            CollectionOrder paymentOrder = this.baseMapper.selectById(req.getId());

            if(ObjectUtils.isEmpty(paymentOrder)){
                throw new BizException(ResultCode.ORDER_NOT_EXIST);
            }
            // 判断交易回调状态
            if(paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.SUCCESS.getCode()) || paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.MANUAL_SUCCESS.getCode())){
                throw new BizException(ResultCode.ORDER_ALREADY_CALLBACK);
            }else {
                result = asyncNotifyService.sendWithDrawSuccessCallbackWithRecordRequest(paymentOrder.getMerchantOrder(), "2");
            }
        }catch (Exception e){
            return result;
        }
        return result;
    }

    @Override
    public RestResult getCollectionOrderInfoByOrderNo(String merchantOrder) {
        //根据订单号查询代收订单详情
        QueryWrapper<CollectionOrder> collectionOrderQueryWrapper = new QueryWrapper<>();
        collectionOrderQueryWrapper.select("merchant_code", "amount", "collected_amount", "order_rate", "currency",
                "create_time", "callback_time", "order_status", "pay_type", "cost", "third_code").eq("merchant_order", merchantOrder);
        CollectionOrder collectionOrder = getOne(collectionOrderQueryWrapper);

        if (collectionOrder != null) {
            CollectionOrderInfoVo collectionOrderInfoVo = new CollectionOrderInfoVo();
            BeanUtil.copyProperties(collectionOrder, collectionOrderInfoVo);

            //通过商户号查询商户名称
            QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
            merchantInfoQueryWrapper.select("username").eq("code", collectionOrder.getMerchantCode());
            MerchantInfo merchantInfo = merchantInfoService.getOne(merchantInfoQueryWrapper);
            if (merchantInfo != null) {
                collectionOrderInfoVo.setUsername(merchantInfo.getUsername());
            }
            System.out.println("collectionOrderInfoVo: " + collectionOrderInfoVo);

            //通过三方代码 查询支付通道名称

            //匹配支付类型枚举值 将支付类型名称返回给前端
            collectionOrderInfoVo.setPayType(PayTypeEnum.getNameByCode(collectionOrder.getPayType()));
            return RestResult.ok(collectionOrderInfoVo);
        } else {

            return RestResult.failed("该笔订单不存在");
        }

    }

    /*
     * 查询下拉列表数据(币种,支付类型)
     * */
    @Override
    public RestResult selectList() {
        //获取当前用户的商户ID
        Long currentUserId = UserContext.getCurrentUserId();


        //查询该商户存在的币种和支付类型
        //币种  一个商户只对应一个币种
        //查询该商户的币种字段
        QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
        merchantInfoQueryWrapper.select("currency").eq("id", currentUserId);
        MerchantInfo merchantInfo = merchantInfoService.getOne(merchantInfoQueryWrapper);

        selectListVo selectListVo = new selectListVo();

        if (merchantInfo != null) {
            //设置币种
            JSONObject currencyJson = new JSONObject();
            currencyJson.put("value", merchantInfo.getCurrency());
            currencyJson.put("label", merchantInfo.getCurrency());
            ArrayList<JSONObject> currencyList = new ArrayList<>();
            currencyList.add(currencyJson);
            selectListVo.setCurrency(currencyList);


            return RestResult.ok(selectListVo);
        } else {
            return RestResult.failed("商户不存在");
        }
    }

    /*
     * 查询最接近给定数字的前10个元素
     * p1 代付池金额列表
     * p2 充值金额
     * p3 列表推荐个数
     * */
    @Override
    public List<Map.Entry<String, Integer>> findClosestValues(Map<String, Integer> map, int collectionAmount, int count) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>(); // 返回一个空列表表示原始 Map 为空
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());

        // 对 Map.Entry 进行排序，根据值与目标数字的差值
        entries.sort(Comparator.comparingInt(entry -> Math.abs(entry.getValue() - collectionAmount)));

        // 返回前 count 个 Map.Entry
        return entries.subList(0, Math.min(count, entries.size()));
    }

    /**
     * 根据订单号获取买入订单
     *
     * @param platformOrder
     * @return {@link CollectionOrder}
     */
    @Override
    public CollectionOrder getCollectionOrderByPlatformOrder(String platformOrder) {
        return lambdaQuery().eq(CollectionOrder::getPlatformOrder, platformOrder).or().eq(CollectionOrder::getMerchantOrder, platformOrder).one();
    }

    /**
     * 根据会员id 查看进行中的买入订单数量
     *
     * @param memberId
     */
    @Override
    public CollectionOrder countActiveBuyOrders(String memberId) {
        return lambdaQuery().in(
                        CollectionOrder::getOrderStatus,
                        OrderStatusEnum.BE_PAID.getCode())//待支付
                .eq(CollectionOrder::getMemberId, memberId).one();
    }

    /**
     * 根据会员id 获取待支付和支付超时的买入订单
     *
     * @param memberId
     * @return {@link CollectionOrder}
     */
    @Override
    public CollectionOrder getPendingBuyOrder(String memberId) {
        return lambdaQuery().eq(CollectionOrder::getMemberId, memberId).eq(CollectionOrder::getOrderStatus, OrderStatusEnum.BE_PAID.getCode()).one();
    }

    /**
     * 获取买入订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link BuyOrderDetailsVo}>
     */
    @Override
    public RestResult<BuyOrderDetailsVo> getBuyOrderDetails(PlatformOrderReq platformOrderReq) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null){
            log.error("查询买入订单详情失败: 获取会员信息失败: {}", memberInfo);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        CollectionOrder collectionOrder = getCollectionOrderByPlatformOrder(platformOrderReq.getPlatformOrder());

        //返回数据vo
        BuyOrderDetailsVo buyOrderDetailsVo = new BuyOrderDetailsVo();

        if (Objects.nonNull(collectionOrder)) {
            BeanUtil.copyProperties(collectionOrder, buyOrderDetailsVo);
            String kycId = collectionOrder.getKycId();
            if (Objects.nonNull(kycId) && StringUtils.isNotEmpty(kycId) && StringUtils.isNotEmpty(kycId.trim())) {
                KycPartners kycPartners = this.kycPartnersMapper.selectById(Integer.valueOf(kycId));
                if (Objects.isNull(kycPartners)) {
                    return RestResult.failed("KYC info query failed");
                }
                buyOrderDetailsVo.setKycAccount(kycPartners.getAccount());
                buyOrderDetailsVo.setKycBankName(kycPartners.getBankName());
            }
            buyOrderDetailsVo.setMemberType(collectionOrder.getMemberType());
        }

        //兼容取消原因和失败原因
        if (buyOrderDetailsVo.getRemark() == null){
            buyOrderDetailsVo.setRemark(buyOrderDetailsVo.getCancellationReason());
        }

//        //判断如果订单状态是手动完成 那么改为已完成
//        if (buyOrderDetailsVo.getOrderStatus().equals(OrderStatusEnum.MANUAL_COMPLETION.getCode())){
//            buyOrderDetailsVo.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
//        }

        //是否经过申诉
        if (collectionOrder.getAppealTime() != null){
            buyOrderDetailsVo.setIsAppealed(1);
        }

        //设置待支付剩余时间
        buyOrderDetailsVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(platformOrderReq.getPlatformOrder()));

        //设置确认中 剩余时间
        buyOrderDetailsVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(platformOrderReq.getPlatformOrder()));

        //判断如果订单是支付中状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
        if (buyOrderDetailsVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (buyOrderDetailsVo.getPaymentExpireTime() == null || buyOrderDetailsVo.getPaymentExpireTime() < 1)){
            buyOrderDetailsVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
        }

        log.info("获取买入订单详情成功: 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), platformOrderReq, buyOrderDetailsVo);

        return RestResult.ok(buyOrderDetailsVo);
    }

    /**
     * 根据IP获取买入订单
     *
     * @param ip
     * @return
     */
    @Override
    public List<CollectionOrder> getCollectOrderByByIp(String ip) {
        LocalDate localDate = LocalDate.now().minusMonths(6);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        return lambdaQuery().eq(CollectionOrder::getClientIp, ip).ge(CollectionOrder::getCreateTime, startOfDay)
                .select(CollectionOrder::getMemberId, CollectionOrder::getPlatformOrder)
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
        LambdaUpdateChainWrapper<CollectionOrder> updateWrapper = lambdaUpdate().in(CollectionOrder::getPlatformOrder, platformOrders);
        if (RiskTagEnum.BLACK_IP.getCode().equals(riskTag)) {
            updateWrapper.set(CollectionOrder::getRiskTagBlack, 1);
        }else if(RiskTagEnum.Normal.getCode().equals(riskTag)){
            updateWrapper.set(CollectionOrder::getRiskTagBlack, 0);
        }else {
            return;
        }
        updateWrapper.update();

    }

    @Override
    public CallBackDetailDTO callBackDetail(CollectionOrderIdReq req) {
        CollectionOrder one = lambdaQuery().eq(CollectionOrder::getId, req.getId()).one();
        CallBackDetailDTO dto = new CallBackDetailDTO();
        BeanUtil.copyProperties(one, dto);
        return dto;
    }

    @Override
    public RestResult<MemberAccountChangeDetailResponseVO> selectByOrderNo(String orderNo) {
        CollectionOrder collectionOrder = this.lambdaQuery()
                .eq(CollectionOrder::getPlatformOrder, orderNo)
                .one();
        if (Objects.isNull(collectionOrder)) {
            return RestResult.failed("Order does not exist");
        }
        // 获取KYC信息
        String kycId = collectionOrder.getKycId();
        KycPartners kycPartners = Objects.nonNull(kycId) && StringUtils.isNotEmpty(kycId) ? this.kycPartnersMapper.selectById(kycId) : null;
        return RestResult.ok(
                MemberAccountChangeDetailResponseVO.builder()
                        .amount(collectionOrder.getAmount())
                        .changeType(MemberAccountChangeEnum.RECHARGE.getCode())
                        .orderNo(orderNo)
                        .utr(collectionOrder.getUtr())
                        .upiId(collectionOrder.getUpiId())
                        .upiName(collectionOrder.getUpiName())
                        .remark(collectionOrder.getRemark())
                        .completionTime(collectionOrder.getCompletionTime())
                        .kycAccount(Objects.isNull(kycPartners) ? null : kycPartners.getAccount())
                        .kycBankName(Objects.isNull(kycPartners) ? null : kycPartners.getBankName())
                        .build()
        );
    }


}
