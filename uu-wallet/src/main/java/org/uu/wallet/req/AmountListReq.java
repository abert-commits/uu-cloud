package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ApiModel(description = "webSocket获取金额列表消息参数")
public class AmountListReq {

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    @Pattern(regexp = "^[A-Za-z0-9]{1,40}$", message = "Invalid format for member ID")
    private String userId;

    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    @DecimalMin(value = "0.00", message = "Invalid format for minimum amount")
    private BigDecimal minimumAmount;

    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    @DecimalMin(value = "0.00", message = "Invalid format for minimum amount")
    private BigDecimal maximumAmount;

    //查询页码
    @ApiModelProperty(value = "查询页码, 默认查询第一页")
    @Min(value = 0, message = "Invalid page number format")
    private Long pageNo = Long.valueOf(1);

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式 取值说明: 1: 印度银行卡, 3: 印度UPI")
    private String paymentType;
}
