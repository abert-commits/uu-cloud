package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;

/**
 * 交易配置表
 */
@Data
@ApiModel(description = "配置信息")
public class TradeConfigUpdateReq extends PageRequest {

    private Long id;


    /**
     * 支付过期时间
     */
    @ApiModelProperty("支付过期时间")
    private Integer rechargeExpirationTime;

    /**
     * 收银台过期时间
     */
    @ApiModelProperty("收银台过期时间")
    private Integer cashierExpirationTime;

    /**
     * 失败次数
     */
    @ApiModelProperty("失败次数")
    private Integer numberFailures;

    /**
     * 禁用买入时间
     */
    @ApiModelProperty("禁用买入时间")
    private Integer disabledTime;

    /**
     * USDT买入金额限制最小值
     */
    @ApiModelProperty("USDT买入金额限制最小值")
    private BigDecimal minAntUsdtDepositAmount;


    /**
     * 买入奖励范围最小值
     */
    @ApiModelProperty("买入奖励范围最小值")
    private BigDecimal buyRewardRatioMin;
    /**
     * 买入奖励范围最大值
     */
    @ApiModelProperty("买入奖励范围最大值")
    private BigDecimal buyRewardRatioMax;


    /* ------卖出参数-------/

     /**
     * 卖出奖励范围比例
     */
    @ApiModelProperty("最小卖出奖励范围比例")
    private BigDecimal sellRewardRatio;

    /**
     * 委托最低金额
     */
    @ApiModelProperty("委托最低金额")
    private BigDecimal minimumDelegationAmount;


    /* -----------预警参数配置-------/
    /**
     *
     * 短信余额报警阈值
     */
    @ApiModelProperty("短信余额报警阈值")
    private BigDecimal messageBalanceThreshold;

    /**
     * 商户订单未产生预警
     */
    @ApiModelProperty("商户订单未产生预警")
    private Integer merchantOrderUncreatedTime;

    /* -------------平台分红配置-------/
     /* -------------汇率参数配置-------/
     /**
     * INR买入比例
     */
    @ApiModelProperty("INR买入比例")
    private BigDecimal buyInrRatio;

    /**
     * USDT汇率
     */
    @ApiModelProperty("USDT汇率")
    private BigDecimal usdtCurrency;

    /* -------------其他参数配置-------/


    /**
     * 最小银行卡号长度
     */
    @ApiModelProperty("最小银行卡号长度")
    private Integer minBankCodeNumber;

    /**
     * 最大银行卡号长度
     */
    @ApiModelProperty("最大银行卡号长度")
    private Integer maxBankCodeNumber;

    /**
     * USDT归集最低金额
     */
    @ApiModelProperty("USDT归集最低金额")
    private BigDecimal minUsdtCollectionAmount;

    @ApiModelProperty("usdt大额代付订单限额")
    private BigDecimal usdtAmountPaymentLimit;

    @ApiModelProperty("inr大额代付订单限额")
    private BigDecimal inrAmountPaymentLimit;

    @ApiModelProperty("trx大额代付订单限额")
    private BigDecimal trxAmountPaymentLimit;

    @ApiModelProperty("下级卖出返佣比例(%,全局统一)")
    private BigDecimal nextOneSellCommissionRatio;

    @ApiModelProperty("下下级卖出返佣比例(%,全局统一)")
    private BigDecimal nextTwoSellCommissionRatio;

    @ApiModelProperty("下级买入返佣比例(%,全局统一)")
    private BigDecimal nextOneBuyCommissionRatio;

    @ApiModelProperty("下下级买入返佣比例(%,全局统一)")
    private BigDecimal nextTwoBuyCommissionRatio;

    @ApiModelProperty("买入奖励比例(%,全局统一)")
    private BigDecimal buyRewardRatio;
}