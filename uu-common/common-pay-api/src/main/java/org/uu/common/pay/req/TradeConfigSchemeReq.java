package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;

/**
 * 交易配置方案表
 *
 *
 * @author Administrator
 */
@Data
@ApiModel(description = "配置方案信息")
public class TradeConfigSchemeReq extends PageRequest {

    @ApiModelProperty("主键")
     private Long id;

    /**
     * 最大买入金额
     */
    @ApiModelProperty("最大买入金额")
    private BigDecimal schemeMaxPurchaseAmount;

    /**
     * 最小买入金额
     */
    @ApiModelProperty("最小买入金额")
    private BigDecimal schemeMinPurchaseAmount;

    /**
     * 最大卖出金额
     */
    @ApiModelProperty("最大卖出金额")
    private BigDecimal schemeMaxSellAmount;

    /**
     * 最小卖出金额
     */
    @ApiModelProperty("最小卖出金额")
    private BigDecimal schemeMinSellAmount;

    /**
     * 同时卖出最多订单
     */
    @ApiModelProperty("同时卖出最多订单")
    private Integer schemeMaxSellOrderNum;

    /**
     * 卖出奖励比例
     */
    @ApiModelProperty("卖出奖励比例")
    private BigDecimal schemeSalesBonusProportion;

    /**
     * 卖出奖励金额日限制
     */
    @ApiModelProperty("卖出奖励金额日限制")
    private BigDecimal schemeSalesBonusAmountLimit;

    /**
     * 卖出奖励笔数日限制
     */
    @ApiModelProperty("卖出奖励笔数日限制")
    private Integer schemeSalesBonusNumLimit;

    /**
     * 确认超时时间
     */
    @ApiModelProperty("确认超时时间")
    private Integer schemeConfirmExpirationTime;

    /**
     * 卖出匹配时长
     */
    @ApiModelProperty("卖出匹配时长")
    private Integer schemeSellMatchingDuration;
}