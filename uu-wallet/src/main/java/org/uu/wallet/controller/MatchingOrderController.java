package org.uu.wallet.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.DurationCalculatorUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


/**
 * @author Admin
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "撮合列表信息控制器")
@RequestMapping(value = {"/api/v1/matchingOrder", "/matchingOrder"})
@ApiIgnore
public class MatchingOrderController {
    private final IMatchingOrderService matchingOrderService;
    private final IAppealOrderService appealOrderService;
    private final PaymentOrderMapper paymentOrderMapper;
    private final CollectionOrderMapper collectionOrderMapper;
    private final ISellService sellService;
    private final MemberInfoMapper memberInfoMapper;
    private final AmountChangeUtil amountChangeUtil;
    private final ITradeConfigService tradeConfigService;
    private final AppealOrderMapper appealOrderMapper;
    @Autowired
    private IMerchantInfoService merchantInfoService;

    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 1000;


    @PostMapping("/relationOrderList")
    @ApiOperation(value = "查询关联订单信息")
    public RestResult<List<RelationOrderDTO>> relationOrderList(@RequestBody @ApiParam RelationshipOrderReq req) {
        Page<RelationOrderDTO> result = matchingOrderService.relationOrderList(req);
        PageReturn<RelationOrderDTO> payConfigPage = new PageReturn<>();
        payConfigPage.setList(result.getRecords());
        payConfigPage.setPageSize(req.getPageSize());
        payConfigPage.setTotal(result.getTotal());
        payConfigPage.setPageNo(req.getPageNo());
        return RestResult.page(payConfigPage);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改")
    public RestResult<MatchingOrderDTO> update(@Validated @RequestBody MatchingOrderReq req) {

        MatchingOrderDTO matchingOrderDTO = matchingOrderService.update(req);
        return RestResult.ok(matchingOrderDTO);
    }

    @PostMapping("/appealDetail")
    @ApiOperation(value = "申诉详情")
    public RestResult<MatchingOrderVoucherDTO> appealDetail(@Validated @RequestBody MatchingOrderIdReq req) {
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        matchingOrder = matchingOrderService.getById(matchingOrder);
        List<AppealOrder> list = appealOrderService.lambdaQuery().eq(AppealOrder::getWithdrawOrderNo, matchingOrder.getPaymentPlatformOrder()).or().eq(AppealOrder::getRechargeOrderNo, matchingOrder.getPaymentPlatformOrder()).list();
        AppealOrderDTO appealOrderDTO = new AppealOrderDTO();
        if (CollectionUtils.isEmpty(list)) {
            return RestResult.ok();
        }
        BeanUtils.copyProperties(list.get(0), appealOrderDTO);
        MatchingOrderVoucherDTO matchingOrderVoucherDTO = new MatchingOrderVoucherDTO();
        BeanUtils.copyProperties(matchingOrder, matchingOrderVoucherDTO);
        matchingOrderVoucherDTO.setSubmitTime(appealOrderDTO.getCreateTime());
        matchingOrderVoucherDTO.setPicInfo(appealOrderDTO.getPicInfo());
        matchingOrderVoucherDTO.setVideoUrl(appealOrderDTO.getVideoUrl());
        matchingOrderVoucherDTO.setReason(appealOrderDTO.getReason());
        return RestResult.ok(matchingOrderVoucherDTO);
    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "充值详情")
    public RestResult<MatchingOrderInfoDTO> getInfo(@Validated @RequestBody MatchingOrderIdReq req) {
        MatchingOrderInfoDTO matchingOrderDTO = matchingOrderService.getInfo(req);
        return RestResult.ok(matchingOrderDTO);
    }


    @PostMapping("/getMatchingOrderTotal")
    @ApiOperation(value = "撮合列表统计")
    public RestResult<MatchingOrderDTO> getMatchingOrderTotal(@Validated @RequestBody MatchingOrderReq req) {
        MatchingOrderDTO matchingOrderDTO = matchingOrderService.getMatchingOrderTotal(req);
        return RestResult.ok(matchingOrderDTO);
    }

//    @PostMapping("/pay")
//    @ApiOperation(value = "已支付")
//    @Transactional(rollbackFor = Exception.class)
//    public RestResult<MatchingOrderDTO> pay(@Validated @RequestBody MatchingOrderAppealReq req) {
//        MatchingOrder matchingOrder = new MatchingOrder();
//        BeanUtils.copyProperties(req, matchingOrder);
//        String currentUserName = UserContext.getCurrentUserName();
//        matchingOrder = matchingOrderService.getById(matchingOrder);
//        if (req.getOrderActualAmount().longValue() > matchingOrder.getOrderSubmitAmount().longValue()) {
//            throw new BizException(ResultCode.AMOUNT_ERROR1);
//        }
//        log.info("BI后台已支付,卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
//        AppealOrder appealOrder = appealOrderMapper.queryAppealOrderByNo(matchingOrder.getPaymentPlatformOrder());
//        //获取买入会员信息(加锁)
//        MemberInfo buyMemberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(matchingOrder.getCollectionMemberId()));
//
//            matchingOrder.setRemark(req.getRemark());
//            matchingOrder.setCompletedBy(currentUserName);
//            matchingOrder.setAppealTime(LocalDateTime.now(ZoneId.systemDefault()));
//            matchingOrder.setOrderActualAmount(req.getOrderActualAmount());
//            matchingOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//            matchingOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//            matchingOrder.setUpdateBy(UserContext.getCurrentUserName());
//            matchingOrder.setPaymentTime(LocalDateTime.now(ZoneId.systemDefault()));
//            matchingOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(matchingOrder.getCreateTime(), LocalDateTime.now(ZoneId.systemDefault())));
//
//            matchingOrderService.updateById(matchingOrder);
//
//            PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(matchingOrder.getPaymentPlatformOrder());
//            paymentOrder.setUpdateBy(currentUserName);
//            paymentOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//            paymentOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//            paymentOrder.setActualAmount(req.getOrderActualAmount());
//            paymentOrder.setRemark(req.getRemark());
//            paymentOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(paymentOrder.getCreateTime(), LocalDateTime.now()));
//
//            CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(matchingOrder.getCollectionPlatformOrder());
//            collectionOrder.setUpdateBy(currentUserName);
//            collectionOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//            collectionOrder.setCompletedBy(currentUserName);
//            collectionOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//            collectionOrder.setActualAmount(req.getOrderActualAmount());
//            collectionOrder.setRemark(req.getRemark());
//            collectionOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(collectionOrder.getCreateTime(), LocalDateTime.now()));
//
//            TradeConfig tradeConfig = tradeConfigService.getById(1);
//            MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(matchingOrder.getPaymentMemberId()));
//            //生成奖励
//            //查看会员如果有单独配置卖出奖励 那么就读取单独配置的卖出奖励
//            if (memberInfo.getSellBonusProportion() != null && memberInfo.getSellBonusProportion().compareTo(new BigDecimal(0)) > 0) {
//                paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(memberInfo.getSellBonusProportion().toString()).divide(BigDecimal.valueOf(100)))));
//                log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(该会员单独配置的奖励): {}", paymentOrder.getBonus());
//            } else {
//                //判断该会员是钱包会员还是商户会员
//                if (MemberTypeEnum.WALLET_MEMBER.getCode().equals(memberInfo.getMemberType())) {
//                    //钱包会员
//                    //会员没有单独配置卖出奖励, 获取配置表奖励比例 并计算出改笔订单奖励值
//                    if (tradeConfig.getMemberSalesBonus() != null && tradeConfig.getMemberSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                        paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMemberSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                        log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                    }
//                } else {
//                    //判断该商户是否单独配置了奖励比例 如果是的话 就直接取该商户单独配置的奖励
//
//                    //获取商户信息
//                    MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(memberInfo.getMerchantCode());
//
//                    if (merchantInfo != null) {
//
//                        //该商户单独配置的卖出奖励不为null并且大于0
//                        if (merchantInfo.getWithdrawalRewards() != null && merchantInfo.getWithdrawalRewards().compareTo(new BigDecimal(0)) > 0) {
//                            paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(merchantInfo.getWithdrawalRewards().toString()).divide(BigDecimal.valueOf(100)))));
//                        } else {
//                            //商户会员 该商户没有单独配置卖出奖励 那么读取默认奖励
//                            if (tradeConfig.getMerchantSalesBonus() != null && tradeConfig.getMerchantSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                                paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMerchantSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                                log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                            }
//                        }
//                    } else {
//                        //就算商户不存在 也要按默认配置取计算奖励
//                        if (tradeConfig.getMerchantSalesBonus() != null && tradeConfig.getMerchantSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                            paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMerchantSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                            log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                        }
//                    }
//                }
//            }
//            //设置奖励 如果会员单独配置了买入奖励 那么才加上买入奖励 默认是没有奖励的
//            if (buyMemberInfo.getBuyBonusProportion() != null && buyMemberInfo.getBuyBonusProportion().compareTo(new BigDecimal(0)) > 0) {
//                collectionOrder.setBonus(req.getOrderActualAmount().multiply((buyMemberInfo.getBuyBonusProportion().divide(BigDecimal.valueOf(100)))));
//                log.info("BI后台添加会员买入奖励: 会员信息{}, 买入奖励金额: {}", buyMemberInfo, collectionOrder.getBonus());
//            } else {
//                //判断如果是商户会员 并且专门配置了买入奖励 那么才有买入奖励
//                if (!MemberTypeEnum.WALLET_MEMBER.getCode().equals(buyMemberInfo.getMemberType())) {
//
//                    //商户会员
//                    //获取商户信息
//                    MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(buyMemberInfo.getMerchantCode());
//
//                    if (merchantInfo != null) {
//                        //判断该商户是否配置了买入奖励
//                        if (merchantInfo.getRechargeReward() != null && merchantInfo.getRechargeReward().compareTo(new BigDecimal(0)) > 0) {
//                            collectionOrder.setBonus(req.getOrderActualAmount().multiply((merchantInfo.getRechargeReward().divide(BigDecimal.valueOf(100)))));
//                            log.info("BI后台添加会员买入奖励, 商户单独配置了买入奖励, 商户信息: {}, 会员信息{}, 买入奖励金额: {}", merchantInfo, buyMemberInfo, collectionOrder.getBonus());
//                        }
//                    }
//            }
//
//
//
//            if (!ObjectUtils.isEmpty(appealOrder) && appealOrder.getAppealStatus().equals(1)) {
//                log.info("已支付更新申诉订单");
//
//                matchingOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//                matchingOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//
//                paymentOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//                paymentOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//
//                collectionOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//                collectionOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//
//                appealOrder.setAppealStatus(2);
//                appealOrderMapper.updateById(appealOrder);
//
//            }
//            matchingOrder.setStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//            matchingOrderService.updateById(matchingOrder);
//            paymentOrder.setOrderStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//            paymentOrderMapper.updateById(paymentOrder);
//
//            collectionOrder.setOrderStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//            collectionOrderMapper.updateById(collectionOrder);
//        }
//
//
//        if (matchingOrder.getOrderSubmitAmount().longValue() > matchingOrder.getOrderActualAmount().longValue()) {
//            // 金额错误补差价
//            BigDecimal changeAmount = matchingOrder.getOrderSubmitAmount().subtract(matchingOrder.getOrderActualAmount());
//            String payType = ObjectUtils.isNotEmpty(matchingOrder.getPayType()) ? matchingOrder.getPayType() : "3";
//            amountChangeUtil.insertMemberChangeAmountRecord(matchingOrder.getPaymentMemberId(), changeAmount, ChangeModeEnum.ADD, CurrenceEnum.ARB.getCode(), matchingOrder.getPaymentPlatformOrder(), MemberAccountChangeEnum.AMOUNT_ERROR, req.getUpdateBy(), payType);
//        }
//
//        if (appealOrder != null) {
//            boolean appealResult = true;
//            boolean valueNotNull = appealOrder.getActualAmount() != null && appealOrder.getOrderAmount() != null;
//            // 买家支付金额小于订单金额, 判定责任方在买家
//            if (DisplayAppealTypeEnum.AMOUNT_INCORRECT.getCode().equals(String.valueOf(appealOrder.getDisplayAppealType())) && valueNotNull && appealOrder.getActualAmount().compareTo(appealOrder.getOrderAmount()) < 0) {
//                appealResult = false;
//                log.info("BI后台已支付,买家支付金额小于订单金额, 判定责任方在买家,卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
//            }
//            // 变更会员信用分
//            appealOrderService.changeCreditScore(appealResult, matchingOrder.getCollectionMemberId(), matchingOrder.getPaymentMemberId(), appealOrder);
//        } else {
//            log.info("BI后台已支付,未查询到申述单无需更新信用分,卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
//        }
//
//
//        return RestResult.ok();
//    }

    @PostMapping("/nopay")
    @ApiOperation(value = "未支付")
    public RestResult<MatchingOrderDTO> nopay(@Validated @RequestBody MatchingOrderAppealReq req) {

        MatchingOrderDTO matchingOrderDTO = matchingOrderService.nopay(req);
        return RestResult.ok(matchingOrderDTO);
    }

//    @PostMapping("/incorrectTransfer")
//    @Transactional(rollbackFor = Exception.class)
//    @ApiOperation(value = "错误转帐")
//    public RestResult<MatchingOrderDTO> incorrectTransfer(@Validated @RequestBody MatchingOrderAppealReq req) {
//        MatchingOrder matchingOrder = new MatchingOrder();
//        BeanUtils.copyProperties(req, matchingOrder);
//        matchingOrder = matchingOrderService.getById(matchingOrder);
//        if (req.getOrderActualAmount().longValue() > matchingOrder.getOrderSubmitAmount().longValue()) {
//            throw new BizException(ResultCode.AMOUNT_ERROR1);
//        }
//        log.info("BI后台错误金额,卖出订单号-{},买入订单号->{}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getCollectionPlatformOrder());
//        matchingOrder.setRemark(req.getRemark());
//        matchingOrder.setOrderActualAmount(req.getOrderActualAmount());
//        matchingOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//        matchingOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//        matchingOrder.setUpdateBy(UserContext.getCurrentUserName());
//        matchingOrder.setPaymentTime(LocalDateTime.now(ZoneId.systemDefault()));
//        matchingOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(matchingOrder.getCreateTime(), LocalDateTime.now()));
//        matchingOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//        matchingOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//
//        AppealOrder appealOrder = appealOrderMapper.queryAppealOrderByNo(matchingOrder.getPaymentPlatformOrder());
//        if (!ObjectUtils.isEmpty(appealOrder) && (appealOrder.getAppealStatus().equals(1) || appealOrder.getAppealStatus().equals(4))) {
//            log.info("金额错误更新申诉订单");
//            appealOrder.setAppealStatus(2);
//            appealOrderMapper.updateById(appealOrder);
//        }
//        CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(matchingOrder.getCollectionPlatformOrder());
//        collectionOrder.setUpdateBy(req.getUpdateBy());
//        collectionOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//        collectionOrder.setCompletedBy(req.getUpdateBy());
//        collectionOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//        collectionOrder.setActualAmount(req.getOrderActualAmount());
//        collectionOrder.setRemark(req.getRemark());
//        collectionOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(collectionOrder.getCreateTime(), LocalDateTime.now(ZoneId.systemDefault())));
//        collectionOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//        collectionOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//        //collectionOrderMapper.updateById(collectionOrder);
//
//
//        PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(matchingOrder.getPaymentPlatformOrder());
//        paymentOrder.setUpdateBy(req.getUpdateBy());
//        paymentOrder.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
//        paymentOrder.setCompletionTime(LocalDateTime.now(ZoneId.systemDefault()));
//        paymentOrder.setActualAmount(req.getOrderActualAmount());
//        paymentOrder.setRemark(req.getRemark());
//        paymentOrder.setCompleteDuration(DurationCalculatorUtil.secondsBetween(paymentOrder.getCreateTime(), LocalDateTime.now(ZoneId.systemDefault())));
//        paymentOrder.setAppealReviewBy(UserContext.getCurrentUserName());
//        paymentOrder.setAppealReviewTime(LocalDateTime.now(ZoneId.systemDefault()));
//        // 奖励补差价
//        TradeConfig tradeConfig = tradeConfigService.getById(1);
//
//        //paymentOrderMapper.updateById(paymentOrder);
//        MemberInfo buyMemberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(matchingOrder.getCollectionMemberId()));
//
//        MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(matchingOrder.getPaymentMemberId()));
//        //生成奖励
//        //查看会员如果有单独配置卖出奖励 那么就读取单独配置的卖出奖励
//        if (memberInfo.getSellBonusProportion() != null && memberInfo.getSellBonusProportion().compareTo(new BigDecimal(0)) > 0) {
//            paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(memberInfo.getSellBonusProportion().toString()).divide(BigDecimal.valueOf(100)))));
//            log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(该会员单独配置的奖励): {}", paymentOrder.getBonus());
//        } else {
//            //判断该会员是钱包会员还是商户会员
//            if (MemberTypeEnum.WALLET_MEMBER.getCode().equals(memberInfo.getMemberType())) {
//                //钱包会员
//                //会员没有单独配置卖出奖励, 获取配置表奖励比例 并计算出改笔订单奖励值
//                if (tradeConfig.getMemberSalesBonus() != null && tradeConfig.getMemberSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                    paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMemberSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                    log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                }
//            } else {
//                //判断该商户是否单独配置了奖励比例 如果是的话 就直接取该商户单独配置的奖励
//
//                //获取商户信息
//                MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(memberInfo.getMerchantCode());
//
//                if (merchantInfo != null) {
//
//                    //该商户单独配置的卖出奖励不为null并且大于0
//                    if (merchantInfo.getWithdrawalRewards() != null && merchantInfo.getWithdrawalRewards().compareTo(new BigDecimal(0)) > 0) {
//                        paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(merchantInfo.getWithdrawalRewards().toString()).divide(BigDecimal.valueOf(100)))));
//                    } else {
//                        //商户会员 该商户没有单独配置卖出奖励 那么读取默认奖励
//                        if (tradeConfig.getMerchantSalesBonus() != null && tradeConfig.getMerchantSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                            paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMerchantSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                            log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                        }
//                    }
//                } else {
//                    //就算商户不存在 也要按默认配置取计算奖励
//                    if (tradeConfig.getMerchantSalesBonus() != null && tradeConfig.getMerchantSalesBonus().compareTo(new BigDecimal(0)) > 0) {
//                        paymentOrder.setBonus(req.getOrderActualAmount().multiply((new BigDecimal(tradeConfig.getMerchantSalesBonus().toString()).divide(BigDecimal.valueOf(100)))));
//                        log.info("BI后台卖出处理 生成卖出订单: 会员卖出奖励(后台配置表奖励): {}", paymentOrder.getBonus());
//                    }
//                }
//            }
//        }
//        //设置奖励 如果会员单独配置了买入奖励 那么才加上买入奖励 默认是没有奖励的
//        if (buyMemberInfo.getBuyBonusProportion() != null && buyMemberInfo.getBuyBonusProportion().compareTo(new BigDecimal(0)) > 0) {
//            collectionOrder.setBonus(req.getOrderActualAmount().multiply((buyMemberInfo.getBuyBonusProportion().divide(BigDecimal.valueOf(100)))));
//            log.info("BI后台添加会员买入奖励: 会员信息{}, 买入奖励金额: {}", buyMemberInfo, collectionOrder.getBonus());
//        } else {
//            //判断如果是商户会员 并且专门配置了买入奖励 那么才有买入奖励
//            if (!MemberTypeEnum.WALLET_MEMBER.getCode().equals(buyMemberInfo.getMemberType())) {
//
//                //商户会员
//                //获取商户信息
//                MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(buyMemberInfo.getMerchantCode());
//
//                if (merchantInfo != null) {
//                    //判断该商户是否配置了买入奖励
//                    if (merchantInfo.getRechargeReward() != null && merchantInfo.getRechargeReward().compareTo(new BigDecimal(0)) > 0) {
//                        collectionOrder.setBonus(req.getOrderActualAmount().multiply((merchantInfo.getRechargeReward().divide(BigDecimal.valueOf(100)))));
//                        log.info("BI后台添加会员买入奖励, 商户单独配置了买入奖励, 商户信息: {}, 会员信息{}, 买入奖励金额: {}", merchantInfo, buyMemberInfo, collectionOrder.getBonus());
//                    }
//                }
//            }
//        }
//
//        RestResult restResult = sellService.transactionSuccessHandler(matchingOrder.getPaymentPlatformOrder(), Long.parseLong(matchingOrder.getPaymentMemberId()), paymentOrder, collectionOrder, "2", null);
//        if (!restResult.getCode().equals("1")) {
//            throw new BizException("金额错误失败");
//        }
//
//        collectionOrder.setOrderStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//        collectionOrderMapper.updateById(collectionOrder);
//        paymentOrder.setOrderStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//        paymentOrderMapper.updateById(paymentOrder);
//        matchingOrder.setStatus(OrderStatusEnum.MANUAL_COMPLETION.getCode());
//        matchingOrderService.updateById(matchingOrder);
//        if (matchingOrder.getOrderSubmitAmount().longValue() > matchingOrder.getOrderActualAmount().longValue()) {
//            // 金额错误补差价
//            BigDecimal changeAmount = matchingOrder.getOrderSubmitAmount().subtract(matchingOrder.getOrderActualAmount());
//            String payType = ObjectUtils.isNotEmpty(matchingOrder.getPayType()) ? matchingOrder.getPayType() : "3";
//            amountChangeUtil.insertMemberChangeAmountRecord(matchingOrder.getPaymentMemberId(), changeAmount, ChangeModeEnum.ADD, CurrenceEnum.ARB.getCode(), matchingOrder.getPaymentPlatformOrder(), MemberAccountChangeEnum.AMOUNT_ERROR, req.getUpdateBy(), payType);
//        }
//
//        MatchingOrderDTO matchingOrderDTO = new MatchingOrderDTO();
//        BeanUtils.copyProperties(matchingOrder, matchingOrderDTO);
//        return RestResult.ok(matchingOrderDTO);
//    }


    @PostMapping("/incorrectVoucher")
    @ApiOperation(value = "错误凭证")
    public RestResult<MatchingOrderVoucherDTO> incorrectVoucher(@Validated @RequestBody MatchingOrderIdReq req) {
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        matchingOrder = matchingOrderService.getById(matchingOrder);
        //AppealOrder appealOrder = appealOrderService.lambdaQuery().eq(AppealOrder::getRechargeOrderNo,matchingOrder.getCollectionPlatformOrder()).one();
        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery = ChainWrappers.lambdaQueryChain(collectionOrderMapper);
        CollectionOrder collectionOrder = lambdaQuery.eq(CollectionOrder::getPlatformOrder, matchingOrder.getCollectionPlatformOrder()).one();
        MatchingOrderVoucherDTO matchingOrderDTO = new MatchingOrderVoucherDTO();
        matchingOrderDTO.setPaymentTime(matchingOrder.getPaymentTime());
        matchingOrderDTO.setUpdateBy(matchingOrder.getUpdateBy());
        if (collectionOrder == null) return RestResult.ok(matchingOrderDTO);
        BeanUtils.copyProperties(matchingOrder, matchingOrderDTO);
        matchingOrderDTO.setSubmitTime(collectionOrder.getCreateTime());

        matchingOrderDTO.setPicInfo(collectionOrder.getAmountErrorImage());
        matchingOrderDTO.setVideoUrl(collectionOrder.getAmountErrorVideo());
        matchingOrderDTO.setUtr(collectionOrder.getUtr());

        return RestResult.ok(matchingOrderDTO);
    }


    @PostMapping("/pscheck")
    @ApiOperation(value = "ps检测")
    public RestResult<MatchingOrderVoucherUrlDTO> pscheck(@Validated @RequestBody MatchingOrderIdReq req) {
        MatchingOrder matchingOrder = new MatchingOrder();
        BeanUtils.copyProperties(req, matchingOrder);
        matchingOrder = matchingOrderService.getById(matchingOrder);
        AppealOrder appealOrder = appealOrderService.lambdaQuery().eq(AppealOrder::getRechargeOrderNo, matchingOrder.getCollectionPlatformOrder()).one();
        MatchingOrderVoucherUrlDTO matchingOrderVoucherUrlDTO = new MatchingOrderVoucherUrlDTO();
        BeanUtils.copyProperties(matchingOrder, matchingOrderVoucherUrlDTO);


        return RestResult.ok(matchingOrderVoucherUrlDTO);
    }

    @PostMapping("/manualReview")
    @ApiOperation(value = "人工审核")
    public RestResult manualReview(@Validated @RequestBody MatchingOrderManualReq req) {
        boolean result = matchingOrderService.manualReview(req, sellService);
        return result ? RestResult.ok() : RestResult.failed();
    }
}
