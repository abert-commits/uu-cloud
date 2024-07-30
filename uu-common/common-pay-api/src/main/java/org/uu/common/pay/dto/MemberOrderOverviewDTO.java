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
@ApiModel(description = "买入/卖出统计")
public class MemberOrderOverviewDTO {

    @ApiModelProperty(value = "买入交易笔数")
    private Long memberPayTransNum;

    @ApiModelProperty(value = "买入交易额")
    private BigDecimal memberPayAmount;

    @ApiModelProperty(value = "买入平均交易额")
    private BigDecimal payAverageAmount;

    @ApiModelProperty(value = "卖出交易笔数")
    private Long memberWithdrawTransNum;

    @ApiModelProperty(value = "卖出交易额")
    private BigDecimal memberWithdrawAmount;

    @ApiModelProperty(value = "卖出平均交易额")
    private BigDecimal withdrawAverageAmount;

    @ApiModelProperty(value = "usdt买入交易额")
    private BigDecimal usdtOrderAmount;

    @ApiModelProperty(value = "usdt买入交易笔数")
    private Long usdtOrderNum;

    @ApiModelProperty(value = "usdt买入平均交易额")
    private BigDecimal usdtAverageAmount;

    @ApiModelProperty(value = "交易额对比值")
    private BigDecimal transAmountDiff;

    @ApiModelProperty(value = "交易笔数对比值")
    private Long transNumDiff;

    @ApiModelProperty(value = "平均交易额对比值")
    private BigDecimal averageAmountDiff;




}
