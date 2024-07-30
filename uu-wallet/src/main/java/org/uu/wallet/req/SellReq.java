package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@ApiModel(description = "卖出下单请求参数")
public class SellReq {


    /**
     * UPI收款信息ID
     */
    @ApiModelProperty(value = "UPI收款信息ID")
    @Min(value = 1, message = "upiCollectionInfoId format is incorrect.")
    private Long upiCollectionInfoId;

    /**
     * 卖出数量
     */
    @ApiModelProperty(value = "卖出数量")
    @NotNull(message = "The selling quantity cannot be empty")
    @DecimalMin(value = "0.00", message = "Sell quantity format is incorrect")
    private BigDecimal amount;

    /**
     * 最小限额
     */
    @DecimalMin(value = "0.00", message = "Minimum limit format is incorrect")
    @ApiModelProperty(value = "最小限额")
    private BigDecimal minimumAmount;


    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式, 取值说明: 1: 银行卡 3: UPI, 5: 银行卡和UPI")
    @NotBlank(message = "payType cannot be empty")
    @Pattern(regexp = "^[0-9]+$", message = "payType format is incorrect, only digits are allowed.")
    private String payType;


    /**
     * 银行卡收款信息ID
     */
    @ApiModelProperty(value = "银行卡收款信息ID")
    @Min(value = 1, message = "bankCollectionInfoId format is incorrect.")
    private Long bankCollectionInfoId;

    /**
     * itoken数量
     */
    @ApiModelProperty(value = "itoken数量")
    private Integer itokenNumber;
    /**
     * 汇率
     */
    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRates;
    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;


}
