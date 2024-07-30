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
@ApiModel(description = "今日USDT订单统计")
public class TodayUsdtOrderOverviewDTO {

    /**
     * 今日usdt买入金额
     */
    @ApiModelProperty(value = "今日usdt买入金额")
    private BigDecimal todayUsdtAmount;

    /**
     * 今日买入iToken金额
     */
    @ApiModelProperty(value = "今日买入iToken金额")
    private BigDecimal todayITokenAmount;

    /**
     * 今日买入笔数
     */
    @ApiModelProperty(value = "今日买入笔数")
    private Long todayUsdtOrderCount;

    /**
     * 今日usdt买入金额
     */
    @ApiModelProperty(value = "今日usdt买入金额")
    private BigDecimal totalUsdtAmount;

    /**
     * 今日买入iToken金额
     */
    @ApiModelProperty(value = "今日买入iToken金额")
    private BigDecimal totalITokenAmount;

    /**
     * 今日买入笔数
     */
    @ApiModelProperty(value = "今日买入笔数")
    private Long totalUsdtOrderCount;
}
