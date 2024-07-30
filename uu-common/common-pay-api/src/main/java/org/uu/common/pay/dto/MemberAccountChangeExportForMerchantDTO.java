package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MemberAccountChangeExportForMerchantDTO {

    /**
     * 平台订单号
     */
    @ApiModelProperty("订单号")
    private String orderNo;
    /**
     * 创建时间
     */
    @ApiModelProperty("订单时间")
    private LocalDateTime createTime;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    @ApiModelProperty("账变类型")
    private String changeType;


    /**
     * 账变前
     */
    @ApiModelProperty("账变前金额")
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    @ApiModelProperty("账变金额")
    private BigDecimal amountChange;

    /**
     * 账变后金额
     */
    @ApiModelProperty("账变后金额")
    private BigDecimal afterChange;



}