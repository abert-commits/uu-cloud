package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "取消订单接口请求参数")
public class CancelOrderReq {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    @NotBlank(message = "Order number cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Order number format is incorrect")
    private String platformOrder;
}
