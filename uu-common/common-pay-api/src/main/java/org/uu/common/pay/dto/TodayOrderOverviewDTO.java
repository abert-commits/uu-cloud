package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author admin
 * @date 2024/3/9 15:37
 */
@Data
@ApiModel(description = "今日订单统计")
public class TodayOrderOverviewDTO {

    /**
     * 今日卖出金额
     */
    @ApiModelProperty(value = "今日卖出金额")
    private BigDecimal todayWithdrawAmount;

    /**
     * 今日卖出手续费
     */
    @ApiModelProperty(value = "今日卖出手续费")
    private BigDecimal todayWithdrawCommission;

    /**
     * 今日卖出成功笔数
     */
    @ApiModelProperty(value = "今日卖出成功笔数")
    private Long todayWithdrawFinishNum;

    /**
     * 今日买入金额
     */
    @ApiModelProperty(value = "今日买入金额")
    private BigDecimal todayPayAmount;

    /**
     * 今日手续费
     */
    @ApiModelProperty(value = "今日买入手续费")
    private BigDecimal todayPayCommission;

    /**
     * 今日支付成功笔数
     */
    @ApiModelProperty(value = "今日买入成功笔数")
    private Long todayPayFinishNum;

    /**
     * 今日usdt买入额
     */
    @ApiModelProperty(value = "今日usdt买入额")
    private BigDecimal todayUsdtAmount;

    /**
     * 今日买入成功率
     */
    @ApiModelProperty(value = "今日买入成功率")
    private BigDecimal todayPaySuccessRate;

    @ApiModelProperty(value = "今日买入总订单数")
    private Long todayPayTotalNum;

    @ApiModelProperty(value = "今日卖出总订单数")
    private Long todayWithdrawTotalNum;

    /**
     * 今日卖出成功率
     */
    @ApiModelProperty(value = "今日卖出成功率")
    private BigDecimal todayWithdrawSuccessRate;

    /**
     * 代收
     */
    @ApiModelProperty(value = "商户今日代收交易额")
    private BigDecimal todayMerchantPayAmount;

    /**
     * 商户今日代收交易笔数
     */
    @ApiModelProperty(value = "商户今日代收交易笔数")
    private Long todayMerchantPayTransNum;

    /**
     * 代付
     */
    @ApiModelProperty(value = "商户今日代付交易额")
    private BigDecimal todayMerchantWithdrawAmount;

    /**
     * 商户今日代付交易笔数
     */
    @ApiModelProperty(value = "商户今日代付交易笔数")
    private Long todayMerchantWithdrawTransNum;
}
