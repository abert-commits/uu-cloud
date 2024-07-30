package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易配置表
 *
 *
 */
@Data
@ApiModel(description = "配置信息")
public class TradeConfigBuyDTO implements Serializable {

     private Long id;

    /**
     * 支付过期时间
     */
    @ApiModelProperty("支付过期时间")
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


}