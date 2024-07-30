package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;

/**
 * 交易配置表
 *
 *
 */
@Data
@ApiModel(description = "配置信息")
public class TradeConfigReq extends PageRequest {

     private Long id;

    /**
     * 支付过期时间
     */
    @ApiModelProperty("充值过期时间")
    private Integer rechargeExpirationTime;

    /**
     * 失败次数
     */
    @ApiModelProperty("买入30分失败次数")
    private Integer numberFailures;

    /**
     * 禁用买入时间
     */
    @ApiModelProperty("禁用买入时间")
    private Integer disabledTime;

    /**
     * USDT汇率
     */
    @ApiModelProperty("USDT汇率")
    private BigDecimal usdtCurrency;

    /**
     * 商户会员最大买入金额
     */
    @ApiModelProperty("商户会员最大买入金额")
    private BigDecimal merchantMaxPurchaseAmount;

    /**
     * 钱包用户最大买入金额
     */
    @ApiModelProperty("钱包用户最大买入金额")
    private BigDecimal memberMaxPurchaseAmount;

    /**
     * 商户会员卖出奖励比例
     */
    @ApiModelProperty("商户会员卖出奖励比例")
    private Double merchantSalesBonus;

    /**
     * 钱包用户卖出奖励比例
     */
    @ApiModelProperty("钱包用户卖出奖励比例")
    private Double memberSalesBonus;

    /**
     * 商户会员卖出最多订单数
     */
    @ApiModelProperty("商户会员卖出最多订单数")
    private Integer merchantMaxSellOrderNum;

    /**
     * 钱包用户卖出最多订单数
     */
    @ApiModelProperty("钱包用户卖出最多订单数")
    private Integer memberMaxSellOrderNum;

    /**
     * 钱包用户最多拆单数
     */
    @ApiModelProperty("钱包用户最多拆单数")
    private Integer maxSplitOrderCount;

    /**
     * 商户会员最大卖出金额
     */
    @ApiModelProperty("商户会员最大卖出金额")
    private BigDecimal merchantMaxSellAmount;

    /**
     * 钱包用户最大卖出金额
     */
    @ApiModelProperty("钱包用户最大卖出金额")
    private BigDecimal memberMaxSellAmount;

    /**
     * 钱包用户确认超时时间
     */
    @ApiModelProperty("钱包用户确认超时时间")
    private Integer memberConfirmExpirationTime;

    /**
     * 商户会员确认超时时间
     */
    @ApiModelProperty("商户会员确认超时时间")
    private Integer merchantConfirmExpirationTime;

    /**
     * 商户会员卖出匹配时长
     */
    @ApiModelProperty("商户会员卖出匹配时长")
    private Integer merchantSellMatchingDuration;

    /**
     * 钱包用户卖出匹配时长
     */
    @ApiModelProperty("钱包用户卖出匹配时长")
    private Integer memberSellMatchingDuration;
}