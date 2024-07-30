package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户管理后台首页
 *
 * @author Admin
 */
@Data
@ApiModel(description = "商户管理后台首页")
public class MerchantFrontPageDTO implements Serializable {

    /**
     * 支付订单总数量(下单成功总数量)
     */
    @ApiModelProperty(value = "支付订单总数量(下单成功总数量)")
    private Long payTotalNum;

    /**
     * 支付成功总数量(代收成功总数量)
     */
    @ApiModelProperty(value = "支付成功总数量(代收成功总数量)")
    private Long payFinishTotalNum;

    /**
     * 代付未完成订单总数量(代付下单成功总数量)
     */
    @ApiModelProperty(value = "代付订单总数量(代付下单成功总数量)")
    private Long withdrawTotalNum;

    /**
     * 代付已完成订单数量
     */
    @ApiModelProperty(value = "代付已完成订单总数量")
    private Long withdrawFinishTotalNum;

    /**
     * 支付未回调订单数量
     */
    @ApiModelProperty(value = "支付未回调订单总数量")
    private Long payNotNotifyTotalNum;

    /**
     * 支付回调失败订单数量
     */
    @ApiModelProperty(value = "支付回调失败订单总数量")
    private Long payNotifyFailedTotalNum;

    /**
     * 代付未回调订单数量
     */
    @ApiModelProperty(value = "代付未回调订单总数量")
    private Long withdrawNotNotifyTotalNum;

    /**
     * 代付回调失败订单数量
     */
    @ApiModelProperty(value = "代付回调失败订单总数量")
    private Long withdrawNotifyFailedTotalNum;

    /**
     * 剩余额度
     */
    @ApiModelProperty(value = "剩余额度")
    private BigDecimal remainingBalance;

    /**
     * 支付代付成功笔数
     */
    @ApiModelProperty(value = "支付代付成功总笔数")
    private Long payAndWithdrawSuccessTotalNum;

    /**
     * 总下发金额
     */
    @ApiModelProperty(value = "总下发金额")
    private BigDecimal transferDownAmount;

    /**
     * 总下发次数
     */
    @ApiModelProperty(value = "总下发次数")
    private Long transferDownCount;


    /**
     * 总上分金额
     */
    @ApiModelProperty(value = "总上分金额")
    private BigDecimal transferUpAmount;

    /**
     * 总上分次数
     */
    @ApiModelProperty(value = "总上分次数")
    private Long transferUpCount;

    /**
     * 今日支付总金额
     */
    @ApiModelProperty(value = "今日支付金额(今日代收额)")
    private BigDecimal todayPayAmount;

    /**
     * 今日手续费
     */
    @ApiModelProperty(value = "今日支付手续费")
    private BigDecimal todayPayCommission;

    /**
     * 今日支付成功笔数
     */
    @ApiModelProperty(value = "今日支付成功笔数(今日代收笔数)")
    private Long todayPayFinishNum;

    /**
     * 支付总金额(总代收额)
     */
    @ApiModelProperty(value = "支付总金额(总代收额)")
    private BigDecimal payFinishTotalAmount;

    /**
     * 支付总手续费(代收总手续费)
     */
    @ApiModelProperty(value = "支付总手续费(代收总手续费)")
    private BigDecimal payTotalCommission;

    /**
     * 今日代付金额
     */
    @ApiModelProperty(value = "今日代付金额(今日付收额)")
    private BigDecimal todayWithdrawAmount;

    /**
     * 今日代付手续费
     */
    @ApiModelProperty(value = "今日代付手续费")
    private BigDecimal todayWithdrawCommission;

    /**
     * 今日代付成功笔数(今日付收笔数)
     */
    @ApiModelProperty(value = "今日代付成功笔数(今日付收笔数)")
    private Long todayWithdrawFinishNum;

    /**
     * 代收取消支付订单数量(取消支付)
     */
    @ApiModelProperty(value = "代收取消支付订单数量(取消支付)")
    private Long payCancelNum;

    /**
     * 代收取消订单数量(取消订单)
     */
    @ApiModelProperty(value = "代收取消订单数量(取消订单)")
    private Long payCancelOrderNum;

    /**
     * 代收申诉订单数量(申诉订单)
     */
    @ApiModelProperty(value = "代收申诉订单数量(申诉订单)")
    private Long payAppealNum;

    /**
     * 代收申诉订单数量(申诉订单)
     */
    @ApiModelProperty(value = "代收申诉订单数量(申诉订单)")
    private Long payAppealTotalNum;

    /**
     * 匹配超时订单笔数
     */
    @ApiModelProperty(value = "代付匹配超时订单笔数")
    private Long withdrawOverTimeNum;

    /**
     * 代付取消匹配订单数量
     */
    @ApiModelProperty(value = "代付取消匹配订单数量")
    private Long withdrawCancelMatchNum;

    /**
     * 代付申诉订单数量
     */
    @ApiModelProperty(value = "代付申诉订单数量")
    private Long withdrawAppealNum;

    /**
     * 代付总申诉订单数量
     */
    @ApiModelProperty(value = "代付总申诉订单数量")
    private Long withdrawAppealTotalNum;

    /**
     * 代付成功总金额(总付收额)
     */
    @ApiModelProperty(value = "代付成功总金额(总付收额)")
    private BigDecimal withdrawFinishTotalAmount;

    /**
     * 代付总手续费(总手续费)
     */
    @ApiModelProperty(value = "代付总手续费(总手续费)")
    private BigDecimal withdrawTotalCommission;

    @ApiModelProperty(value = "上次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 登录IP
     */
    @ApiModelProperty(value = "登录IP")
    private String loginIp;

    /**
     * 金额错误订单
     */
    @ApiModelProperty(value = "金额错误订单")
    private Long amountErrorNum;

    /**
     * 今日usdt买入额
     */
    @ApiModelProperty(value = "今日usdt买入额")
    private BigDecimal todayUsdtAmount;

    /**
     * 总USDT买入额
     */
    @ApiModelProperty(value = "总USDT买入额")
    private BigDecimal usdtTotalAmount;

    /**
     * USDT订单数
     */
    @ApiModelProperty(value = "USDT订单数")
    private Long usdtTotalNum;

    /**
     * 匹配成功
     */
    @ApiModelProperty(value = "匹配成功")
    private Long matchSuccessNum;


    /**
     * 买入订单列表
     *
     */
    @ApiModelProperty(value = "买入订单列表")
    private List<BiPaymentOrderDTO> buyList;

    /**
     * 卖出订单列表
     */
    @ApiModelProperty(value = "卖出订单列表")
    private List<BiWithdrawOrderDailyDTO> sellList;


    /**
     * 代收
     */
    @ApiModelProperty(value = "商户今日代收交易额")
    private BigDecimal todayMerchantPayAmount;

    @ApiModelProperty(value = "商户今日代收交易笔数")
    private Long todayMerchantPayTransNum;


    @ApiModelProperty(value = "商户代收交易总金额")
    private BigDecimal merchantPayTotalAmount;


    @ApiModelProperty(value = "商户代收交易总笔数")
    private Long merchantPayTransTotalNum;

    @ApiModelProperty(value = "商户代收今日费率")
    private BigDecimal todayMerchantPayCommission;

    @ApiModelProperty(value = "商户代收总费率")
    private BigDecimal merchantPayTotalCommission;


    /**
     * 代付
     */
    @ApiModelProperty(value = "商户今日代付交易额")
    private BigDecimal todayMerchantWithdrawAmount;


    @ApiModelProperty(value = "商户今日代付交易笔数")
    private Long todayMerchantWithdrawTransNum;


    @ApiModelProperty(value = "商户代付交易总金额")
    private BigDecimal merchantWithdrawTotalAmount;

    @ApiModelProperty(value = "商户代付交易总笔数")
    private Long merchantWithdrawTransTotalNum;

    @ApiModelProperty(value = "商户代付今日费率")
    private BigDecimal todayMerchantWithdrawCommission;

    @ApiModelProperty(value = "商户代付总费率")
    private BigDecimal merchantWithdrawTotalCommission;

    @ApiModelProperty(value = "今日买入总订单数")
    private Long todayPayTotalNum;

    @ApiModelProperty(value = "今日卖出总订单数")
    private Long todayWithdrawTotalNum;

}
