package org.uu.wallet.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
public class SellPlatformOrderReq {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "卖出订单号")
    @NotBlank(message = "Sell order number cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Sell order number format is incorrect")
    private String platformOrder;


    /**
     * 支付密码
     */
    @NotBlank(message = "paymentPassword cannot be empty")
    @Pattern(regexp = "^\\d{4}$", message = "paymentPassword format is incorrect")
    @ApiModelProperty("支付密码")
    private String paymentPassword;
}