package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员账变记录
 *
 * @author
 */
@Data
@ApiModel(description = "会员账变记录")
public class MemberAccountChangeExportForMerchantEnDTO {
    /**
     * 平台订单号
     */
    @ApiModelProperty("orderNo")
    private String orderNo;
    /**
     * 创建时间
     */
    @ApiModelProperty("createTime")
    private LocalDateTime createTime;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    @ApiModelProperty("changeType")
    private String changeType;


    /**
     * 账变前
     */
    @ApiModelProperty("beforeChange")
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    @ApiModelProperty("amountChange")
    private BigDecimal amountChange;

    /**
     * 账变后金额
     */
    @ApiModelProperty("afterChange")
    private BigDecimal afterChange;

}