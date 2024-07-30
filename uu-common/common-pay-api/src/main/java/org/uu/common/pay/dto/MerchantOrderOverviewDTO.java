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
@ApiModel(description = "代收/代付统计")
public class MerchantOrderOverviewDTO {

    @ApiModelProperty(value = "代收交易笔数")
    private Long merchantPayTransNum;

    @ApiModelProperty(value = "代收交易额")
    private BigDecimal merchantPayAmount;

    @ApiModelProperty(value = "代收平均交易额")
    private BigDecimal payAverageAmount;

    @ApiModelProperty(value = "代收手续费")
    private BigDecimal payFee;

    @ApiModelProperty(value = "代付交易笔数")
    private Long merchantWithdrawTransNum;

    @ApiModelProperty(value = "代付交易额")
    private BigDecimal merchantWithdrawAmount;

    @ApiModelProperty(value = "代付平均交易额")
    private BigDecimal withdrawAverageAmount;

    @ApiModelProperty(value = "代收手续费")
    private BigDecimal withdrawFee;

    @ApiModelProperty(value = "交易额对比值")
    private BigDecimal transAmountDiff;

    @ApiModelProperty(value = "交易笔数对比值")
    private Long transNumDiff;

    @ApiModelProperty(value = "平均交易额对比值")
    private BigDecimal averageAmountDiff;

    @ApiModelProperty(value = "手续费对比值")
    private BigDecimal feeAmountDiff;
}
