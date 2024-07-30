package org.uu.wallet.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
public class BuyPlatformOrderReq {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "买入订单号")
    @NotBlank(message = "Buy order number cannot be empty")
    @Pattern(regexp = "^MR\\d{17}\\d{5}$", message = "The buy order number format is incorrect")
    private String platformOrder;
}