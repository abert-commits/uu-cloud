package org.uu.wallet.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
public class PlatformOrderReq {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    @NotBlank(message = "Order number cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Order number format is incorrect")
    private String platformOrder;
}