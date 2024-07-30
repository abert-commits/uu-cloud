package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.AppealOrderDTO;
import org.uu.common.pay.dto.AppealOrderExportDTO;
import org.uu.common.pay.req.AppealOrderIdReq;
import org.uu.common.pay.req.AppealOrderPageListReq;
import org.uu.common.pay.req.MemberInfoCreditScoreReq;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.oss.OssService;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.req.ViewTransactionHistoryReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.FileUtil;
import org.uu.wallet.vo.AppealDetailsVo;
import org.uu.wallet.vo.AppealOrderVo;
import org.uu.wallet.vo.ViewMyAppealVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.uu.wallet.Enum.CreditEventTypeEnum.*;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppealOrderServiceImpl extends ServiceImpl<AppealOrderMapper, AppealOrder> implements IAppealOrderService {

    @Value("${oss.accessKeyId}")
    String accessKeyId;
    @Value("${oss.accessKeySecret}")
    String accessKeySecret;
    @Value("${oss.endpoint}")
    String endpoint;
    @Resource
    AppealOrderMapper appealOrderMapper;
    @Resource
    MatchingOrderMapper matchingOrderMapper;
    @Resource
    PaymentOrderMapper paymentOrderMapper;
    @Resource
    CollectionOrderMapper collectionOrderMapper;

    //从nacos获取配置
    private final ArProperty arProperty;

    private final OssService ossService;

    @Autowired
    private ICollectionOrderService collectionOrderService;

    @Autowired
    private IPaymentOrderService paymentOrderService;
    private final AmountChangeUtil amountChangeUtil;


    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private CollectionInfoMapper collectionInfoMapper;

    private final MemberAccountChangeMapper memberAccountChangeMapper;
    private final MemberInfoMapper memberInfoMapper;
    private static final String URL_HOST = "https://arb-pay.oss-ap-southeast-1.aliyuncs.com/";
    private static final String IMG_PREFIX = "image/";

    private static final String VIDEO_PREFIX = "video/";

    private static final String BUCKET_NAME = "arb-pay";

    @Override
    public AppealOrderVo queryAppealOrder(String orderNo, Integer appealType) throws Exception{

        AppealOrderVo appealOrderVo = new AppealOrderVo();
        CompletableFuture<MatchingOrder> matchingOrderFuture = null;
        CompletableFuture<PaymentOrder> paymentFuture = null;
        CompletableFuture<CollectionOrder> collectionFuture = null;
        CompletableFuture<Void> allFutures = null;
        AppealOrder appealOrder = new AppealOrder();
        MatchingOrder matchingOrder = new MatchingOrder();
        PaymentOrder paymentOrder = null;
        CollectionOrder collectionOrder = null;

        // 查询申诉订单信息
        CompletableFuture<AppealOrder> f1 =  CompletableFuture.supplyAsync(()->{
            return appealOrderMapper.queryAppealOrderByOrderNo(orderNo, appealType);
        });

        // 查询匹配订单记录
        if(appealType.equals(1)){
            matchingOrderFuture = CompletableFuture.supplyAsync(()->{
                LambdaQueryWrapper<MatchingOrder> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MatchingOrder::getPaymentPlatformOrder, orderNo);
                return matchingOrderMapper.selectOne(queryWrapper);
            });
            paymentFuture = CompletableFuture.supplyAsync(()->{
                LambdaQueryWrapper<PaymentOrder> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PaymentOrder::getPlatformOrder, orderNo);
                return paymentOrderMapper.selectOne(queryWrapper);
            });
            allFutures = CompletableFuture.allOf(f1, paymentFuture, matchingOrderFuture);
            allFutures.get();
            appealOrder = f1.get();
            matchingOrder = matchingOrderFuture.get();
            paymentOrder = paymentFuture.get();

            appealOrderVo.setOrderNo(matchingOrder.getPaymentPlatformOrder());
            appealOrderVo.setOrderTime(paymentOrder.getCreateTime());
            appealOrderVo.setAmount(paymentOrder.getAmount());
            appealOrderVo.setPayTime(matchingOrder.getPaymentTime());

        }else {
            matchingOrderFuture = CompletableFuture.supplyAsync(()->{
                LambdaQueryWrapper<MatchingOrder> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MatchingOrder::getCollectionPlatformOrder, orderNo);
                return matchingOrderMapper.selectOne(queryWrapper);
            });
            collectionFuture = CompletableFuture.supplyAsync(()->{
                LambdaQueryWrapper<CollectionOrder> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CollectionOrder::getPlatformOrder, orderNo);
                return collectionOrderMapper.selectOne(queryWrapper);
            });
            allFutures = CompletableFuture.allOf(f1, collectionFuture, matchingOrderFuture);
            allFutures.get();
            appealOrder = f1.get();
            matchingOrder = matchingOrderFuture.get();
            collectionOrder = collectionFuture.get();

            appealOrderVo.setOrderNo(matchingOrder.getCollectionPlatformOrder());
            appealOrderVo.setOrderTime(collectionOrder.getCreateTime());
            appealOrderVo.setAmount(collectionOrder.getAmount());
        }

        appealOrderVo.setAppealTime(appealOrder.getCreateTime());
        appealOrderVo.setUpi(matchingOrder.getUpiId());
        appealOrderVo.setUtr(matchingOrder.getUtr());
        appealOrderVo.setReason(appealOrder.getReason());
        appealOrderVo.setPicInfo(appealOrder.getPicInfo());
        appealOrderVo.setVideoUrl(appealOrder.getVideoUrl());

        return appealOrderVo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppealOrderDTO pay(AppealOrderIdReq req){
        String updateBy = UserContext.getCurrentUserName();
        AppealOrder appealOrde = new AppealOrder();
        appealOrde.setId(req.getId());
        appealOrde = baseMapper.selectById(appealOrde);
        if(appealOrde.getAppealStatus().equals(2) || appealOrde.getAppealStatus().equals(3)){
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
        appealOrde.setAppealStatus(2);
        //  if(appealOrde.getAppealType().equals(AppealTypeEnum.WITHDRAW.getCode())){
        PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(appealOrde.getWithdrawOrderNo());
        if(ObjectUtils.isEmpty(paymentOrder)){
            throw new BizException(ResultCode.WITHDRAW_ORDER_NOT_EXIST);
        }
        paymentOrder.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
        paymentOrderMapper.updateById(paymentOrder);
        //   }else if(appealOrde.getAppealType().equals(AppealTypeEnum.RECHARGE.getCode())){
        CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(appealOrde.getRechargeOrderNo());
        if(ObjectUtils.isEmpty(collectionOrder)){
            throw new BizException(ResultCode.RECHARGE_ORDER_NOT_EXIST);
        }
        collectionOrder.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
        collectionOrderMapper.updateById(collectionOrder);
        String payType = ObjectUtils.isNotEmpty(collectionOrder.getPayType()) ? collectionOrder.getPayType() : "3";
        amountChangeUtil.insertMemberChangeAmountRecord(collectionOrder.getMemberId(),collectionOrder.getActualAmount(), ChangeModeEnum.ADD,"ARB",collectionOrder.getPlatformOrder(), MemberAccountChangeEnum.RECHARGE, updateBy, payType);
        //   }
        baseMapper.updateById(appealOrde);
        AppealOrderDTO appealOrderDTO = new AppealOrderDTO();
        BeanUtils.copyProperties(appealOrde,appealOrderDTO);
        //CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(appealOrde.getRechargeOrderNo());
        // 变更会员信用分
        changeCreditScore(Boolean.TRUE, collectionOrder.getMemberId(), paymentOrder.getMemberId(), appealOrde);
        return appealOrderDTO;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public  AppealOrderDTO nopay(AppealOrderIdReq req){
        String updateBy = UserContext.getCurrentUserName();
        AppealOrder appealOrde = new AppealOrder();
        appealOrde.setId(req.getId());
        appealOrde = baseMapper.selectById(appealOrde);
        if(appealOrde.getAppealStatus().equals(2) || appealOrde.getAppealStatus().equals(3)){
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
        appealOrde.setAppealStatus(3);
        //   if(appealOrde.getAppealType().equals(AppealTypeEnum.WITHDRAW.getCode())){
        PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(appealOrde.getWithdrawOrderNo());
        if(ObjectUtils.isEmpty(paymentOrder)){
            throw new BizException(ResultCode.WITHDRAW_ORDER_NOT_EXIST);
        }
//        String payType = ObjectUtils.isNotEmpty(paymentOrder.getCurrentPayType()) ? paymentOrder.getCurrentPayType() : "3";
        String payType = paymentOrder.getPayType();
        amountChangeUtil.insertMemberChangeAmountRecord(paymentOrder.getMemberId(),paymentOrder.getActualAmount(), ChangeModeEnum.ADD,"ARB",paymentOrder.getPlatformOrder(), MemberAccountChangeEnum.RECHARGE, updateBy, payType);
        paymentOrderMapper.updateById(paymentOrder);
        CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(appealOrde.getRechargeOrderNo());
        if(ObjectUtils.isEmpty(collectionOrder)){
            throw new BizException(ResultCode.RECHARGE_ORDER_NOT_EXIST);
        }
        collectionOrderMapper.updateById(collectionOrder);
        this.baseMapper.deleteById(appealOrde);
//       }else if(appealOrde.getAppealType().equals(AppealTypeEnum.RECHARGE.getCode())){
//           CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(appealOrde.getRechargeOrderNo());
//           collectionOrder.setOrderStatus(OrderStatusEnum.BUY_FAILED.getCode());
//           collectionOrderMapper.updateById(collectionOrder);
//       }

        // 变更会员信用分
        changeCreditScore(Boolean.FALSE, collectionOrder.getMemberId(), paymentOrder.getMemberId(), appealOrde);

        AppealOrderDTO appealOrderDTO = new AppealOrderDTO();
        BeanUtils.copyProperties(appealOrde,appealOrderDTO);
        return appealOrderDTO;
    }



    @SneakyThrows
    @Override
    public PageReturn<AppealOrderDTO> listPage(AppealOrderPageListReq req) {
        Long actualAmountPageTotal = 0L;
        Long orderAmountPageTotal = 0L;
        Page<AppealOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<AppealOrder> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(AppealOrder::getCreateTime);
        LambdaQueryWrapper<AppealOrder> queryWrapper = new QueryWrapper<AppealOrder>()
                .select("IFNULL(sum(actual_amount),0) as actualAmountTotal, IFNULL(sum(order_amount),0) as orderAmountTotal").lambda();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getMid())) {
            lambdaQuery.eq(AppealOrder::getMid, req.getMid());
            queryWrapper.eq(AppealOrder::getMid, req.getMid());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getRechargeOrderNo())) {
            lambdaQuery.eq(AppealOrder::getRechargeOrderNo, req.getRechargeOrderNo());
            queryWrapper.eq(AppealOrder::getRechargeOrderNo, req.getRechargeOrderNo());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getWithdrawOrderNo())) {
            lambdaQuery.eq(AppealOrder::getWithdrawOrderNo, req.getWithdrawOrderNo());
            queryWrapper.eq(AppealOrder::getWithdrawOrderNo, req.getWithdrawOrderNo());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getStatus())) {
            lambdaQuery.eq(AppealOrder::getAppealStatus, req.getStatus());
            queryWrapper.eq(AppealOrder::getAppealStatus, req.getStatus());
        }

        if (req.getCreateTimeStart() != null) {
            lambdaQuery.ge(AppealOrder::getCreateTime, req.getCreateTimeStart());
            queryWrapper.ge(AppealOrder::getCreateTime, req.getCreateTimeStart());
        }

        //--动态查询 结束时间
        if (req.getCreateTimeEnd()!= null) {
            lambdaQuery.le(AppealOrder::getCreateTime,  req.getCreateTimeEnd());
            queryWrapper.le(AppealOrder::getCreateTime,  req.getCreateTimeEnd());
        }
        // 支付类型
        if (req.getPayType() != null) {
            if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_CARD.getCode())){
                lambdaQuery.and(w -> w.or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
                queryWrapper.and(w -> w.or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
            }else if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_UPI.getCode())){
                // 支持upi INDIAN_UPI, INDIAN_CARD_UPI_FIX
                lambdaQuery.and(w -> w.or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
                queryWrapper.and(w -> w.or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(AppealOrder::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
            }else{
                lambdaQuery.eq(AppealOrder::getPayType, req.getPayType());
                queryWrapper.eq(AppealOrder::getPayType, req.getPayType());
            }
        }
        CompletableFuture<AppealOrder> totalFuture = CompletableFuture.supplyAsync(() -> {
            return baseMapper.selectOne(queryWrapper);
        });

        Page<AppealOrder> finalPage = page;
        CompletableFuture<Page<AppealOrder>> resultFuture = CompletableFuture.supplyAsync(() -> {
            return baseMapper.selectPage(finalPage, lambdaQuery.getWrapper());
        });
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(totalFuture, resultFuture);
        allFutures.get();
        page = resultFuture.get();
        AppealOrder appealOrderTotal = totalFuture.get();
        JSONObject extend = new JSONObject();
        extend.put("actualAmountTotal", appealOrderTotal.getActualAmountTotal());
        extend.put("orderAmountTotal", appealOrderTotal.getOrderAmountTotal());
        //baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<AppealOrder> records = page.getRecords();
        List<AppealOrderDTO> list = new ArrayList<AppealOrderDTO>();
        for(AppealOrder appealOrder :records){
            orderAmountPageTotal += appealOrder.getOrderAmount().longValue();
            actualAmountPageTotal += appealOrder.getActualAmount().longValue();
            AppealOrderDTO appealOrderDTO = new AppealOrderDTO();
            BeanUtils.copyProperties(appealOrder,appealOrderDTO);
            list.add(appealOrderDTO);
        }
        extend.put("orderAmountPageTotal", orderAmountPageTotal);
        extend.put("actualAmountPageTotal", actualAmountPageTotal);
        // List<ApplyDistributedDTO> accountChangeVos = walletMapStruct.ApplyDistributedTransform(records);
        return PageUtils.flush(page, list, extend);
    }

    @Override
    public PageReturn<AppealOrderExportDTO> listPageExport(AppealOrderPageListReq req) {
        PageReturn<AppealOrderDTO> appealOrder = listPage(req);

        List<AppealOrderExportDTO> resultList = new ArrayList<>();
        for (AppealOrderDTO appealOrderDTO : appealOrder.getList()) {
            AppealOrderExportDTO appealOrderExportDto = new AppealOrderExportDTO();
            BeanUtils.copyProperties(appealOrderDTO, appealOrderExportDto);
            appealOrderExportDto.setAppealType(AppealTypeEnum.getNameByCode(appealOrderDTO.getAppealType().toString()));
            appealOrderExportDto.setAppealStatus(AppealStatusEnum.getNameByCode(appealOrderDTO.getAppealStatus().toString()));
            appealOrderExportDto.setOrderAmount(appealOrderDTO.getOrderAmount().toString());
            appealOrderExportDto.setActualAmount(appealOrderDTO.getActualAmount().toString());
            resultList.add(appealOrderExportDto);
        }
        Page<AppealOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(appealOrder.getTotal());
        return PageUtils.flush(page, resultList);
    }

    /**
     * 根据买入订单号获取申诉订单
     *
     * @param platformOrder
     * @return {@link AppealOrder}
     */
    @Override
    public AppealOrder getAppealOrderByBuyOrderNo(String platformOrder) {
        return lambdaQuery().eq(AppealOrder::getRechargeOrderNo, platformOrder).last("LIMIT 1").one();
    }


    /**
     * 根据卖出订单号获取申诉订单
     *
     * @param platformOrder
     * @return {@link AppealOrder}
     */
    @Override
    public AppealOrder getAppealOrderBySellOrderNo(String platformOrder) {
        return lambdaQuery().eq(AppealOrder::getWithdrawOrderNo, platformOrder).last("LIMIT 1").one();
    }

    /**
     * 查看订单申诉详情
     *
     * @param platformOrderReq
     * @param type             1: 买入申诉  2: 卖出申诉
     * @return {@link RestResult}<{@link AppealDetailsVo}>
     */
    @Override
    public RestResult<AppealDetailsVo> viewAppealDetails(PlatformOrderReq platformOrderReq, String type) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("查看订单申诉详情失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询申诉订单
        AppealOrder appealOrderByOrderNo = null;

        if (type.equals("1")) {
            //买入申诉订单
            appealOrderByOrderNo = getAppealOrderByBuyOrderNo(platformOrderReq.getPlatformOrder());
        } else if (type.equals("2")) {
            //卖出申诉订单
            appealOrderByOrderNo = getAppealOrderBySellOrderNo(platformOrderReq.getPlatformOrder());
        }

        if (appealOrderByOrderNo == null) {
            log.error("查看订单申诉详情失败, 申诉订单不存在 会员信息: {}, 订单信息: {}", memberInfo, appealOrderByOrderNo);
            return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
        }

        //返回对象
        AppealDetailsVo appealDetailsVo = new AppealDetailsVo();

        String memberId = String.valueOf(memberInfo.getId());

        //查看订单是买入订单还是卖出订单
        if (platformOrderReq.getPlatformOrder().startsWith("MR")) {
            //查询买入订单
            CollectionOrder collectionOrder = collectionOrderService.getCollectionOrderByPlatformOrder(platformOrderReq.getPlatformOrder());


            if (collectionOrder == null || !collectionOrder.getMemberId().equals(memberId)) {
                log.error("查看订单申诉详情失败, 买入订单不存在或该订单不属于该会员, 会员信息: {}, 买入订单信息: {}", memberInfo, collectionOrder);
                return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
            }

            //将买入订单信息赋值给返回对象
            BeanUtils.copyProperties(collectionOrder, appealDetailsVo);

        } else if (platformOrderReq.getPlatformOrder().startsWith("MC")) {
            //查询卖出订单
            PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(platformOrderReq.getPlatformOrder());

            if (paymentOrder == null || !paymentOrder.getMemberId().equals(memberId)) {
                log.error("查看订单申诉详情失败, 卖出订单不存在或该订单不属于该会员, 会员信息: {}, 卖出订单信息: {}", memberInfo, paymentOrder);
                return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
            }

            //将卖出订单信息赋值给返回对象
            BeanUtils.copyProperties(paymentOrder, appealDetailsVo);

        } else {
            return RestResult.failure(ResultCode.ORDER_NUMBER_ERROR);
        }

        appealDetailsVo.setReason(appealOrderByOrderNo.getReason());
        appealDetailsVo.setPicInfo(appealOrderByOrderNo.getPicInfo());
        appealDetailsVo.setVideoUrl(appealOrderByOrderNo.getVideoUrl());

        //根据银行卡号查询收款信息
        if (StringUtils.isNotEmpty(appealDetailsVo.getBankCardNumber())){

            // 创建 LambdaQueryWrapper 实例
            LambdaQueryWrapper<CollectionInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CollectionInfo::getBankCardNumber, appealDetailsVo.getBankCardNumber())
                    .eq(CollectionInfo::getDeleted, 0)
                    .last("LIMIT 1");

            // 执行查询
            CollectionInfo collectionInfo = collectionInfoMapper.selectOne(queryWrapper);

            // 处理查询结果
            if (collectionInfo != null) {
                // 处理收款信息
                appealDetailsVo.setBankName(collectionInfo.getBankName());
                appealDetailsVo.setBankCardOwner(collectionInfo.getBankCardOwner());
                appealDetailsVo.setIfscCode(collectionInfo.getIfscCode());
            }
        }

        log.info("查看订单申诉详情 处理成功 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), appealDetailsVo);

        return RestResult.ok(appealDetailsVo);
    }

    /**
     * 我的申诉
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link ViewMyAppealVo}>>
     */
    @Override
    public RestResult<PageReturn<ViewMyAppealVo>> viewMyAppeal(PageRequestHome req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("查看-我的申诉失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        String memberId = String.valueOf(memberInfo.getId());

        if (req == null) {
            req = new ViewTransactionHistoryReq();
        }

        Page<AppealOrder> pageAppealOrder = new Page<>();
        pageAppealOrder.setCurrent(req.getPageNo());
        pageAppealOrder.setSize(req.getPageSize());

        LambdaQueryChainWrapper<AppealOrder> lambdaQuery = lambdaQuery();

        //查询当前会员的申诉订单或是被申诉订单
        lambdaQuery.eq(AppealOrder::getMid, memberId).or().eq(AppealOrder::getAppealedMemberId, memberId);

        // 倒序排序
        lambdaQuery.orderByDesc(AppealOrder::getId);

        baseMapper.selectPage(pageAppealOrder, lambdaQuery.getWrapper());

        List<AppealOrder> records = pageAppealOrder.getRecords();

        ArrayList<ViewMyAppealVo> viewMyAppealVoList = new ArrayList<>();

        //IPage＜实体＞转 IPage＜Vo＞
        for (AppealOrder appealOrder : records) {

            //返回数据
            ViewMyAppealVo viewMyAppealVo = new ViewMyAppealVo();
            BeanUtils.copyProperties(appealOrder, viewMyAppealVo);

            //判断该笔订单是买入还是卖出
            if (appealOrder.getAppealType() == 2) {
                //买入订单
                //查看申诉id是不是自己的
                if (appealOrder.getMid().equals(memberId)) {
                    //申诉id是自己的 那么显示买入订单号
                    viewMyAppealVo.setPlatformOrder(appealOrder.getRechargeOrderNo());
                } else {
                    //申诉id不是自己的 显示卖出订单号
                    viewMyAppealVo.setPlatformOrder(appealOrder.getWithdrawOrderNo());
                }
            } else {
                //卖出订单
                //查看申诉id是不是自己的
                if (appealOrder.getMid().equals(memberId)) {
                    //申诉id是自己的 显示卖出订单号
                    viewMyAppealVo.setPlatformOrder(appealOrder.getWithdrawOrderNo());
                } else {
                    //申诉id不是自己的 显示买入订单号
                    viewMyAppealVo.setPlatformOrder(appealOrder.getRechargeOrderNo());
                }
            }

            // 调整申诉类型
            if (viewMyAppealVo.getPlatformOrder().startsWith("MC")) {
                viewMyAppealVo.setAppealType(1); // 卖出
            } else if (viewMyAppealVo.getPlatformOrder().startsWith("MR")) {
                viewMyAppealVo.setAppealType(2); // 买入
            }

            viewMyAppealVoList.add(viewMyAppealVo);
        }

        PageReturn<ViewMyAppealVo> flush = PageUtils.flush(pageAppealOrder, viewMyAppealVoList);

        log.info("查看-我的申诉成功: 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), req, flush);

        return RestResult.ok(flush);
    }

    /**
     * 变更会员信用分
     *
     * @param orderSuccess
     * @param buyerId
     * @param sellerId
     * @param appealOrder
     */
    @Override
    public void changeCreditScore(boolean orderSuccess, String buyerId, String sellerId, AppealOrder appealOrder) {
        CreditEventTypeEnum buyerEventType = null;
        CreditEventTypeEnum sellerEventType = null;
        if (orderSuccess) {
            if (buyerId.equals(appealOrder.getMid())) {
                // 买家为申述方
                buyerEventType = APPEAL_SUCCESS;
                sellerEventType = BE_APPEAL_FAILED;
            } else if (sellerId.equals(appealOrder.getMid())) {
                // 卖家为申述方
                buyerEventType = BE_APPEAL_SUCCESS;
                sellerEventType = APPEAL_FAILED;
            }
        } else {
            if (buyerId.equals(appealOrder.getMid())) {
                // 买家为申述方
                buyerEventType = APPEAL_FAILED;
                sellerEventType = BE_APPEAL_SUCCESS;
            } else if (sellerId.equals(appealOrder.getMid())) {
                // 卖家为申述方
                buyerEventType = BE_APPEAL_FAILED;
                sellerEventType = APPEAL_SUCCESS;
            }
        }

        if (buyerEventType == null || sellerEventType == null) {
            log.error("更新信用分, 申述人非买家和买家, 不能更新信息分, 申诉人:{}, 申诉单Id:{}", appealOrder.getMid(), appealOrder.getId());
            return;
        }

        log.info("更新信用分, 根据申诉结果变更, 买方:{}, 卖方:{}", buyerId, sellerId);
        // 更新买家信用分
        memberInfoService.updateCreditScore(MemberInfoCreditScoreReq.builder().id(Long.valueOf(buyerId)).eventType(buyerEventType.getCode()).tradeType(1).build());
        memberInfoService.updateCreditScore(MemberInfoCreditScoreReq.builder().id(Long.valueOf(sellerId)).eventType(sellerEventType.getCode()).tradeType(2).build());
    }
}
