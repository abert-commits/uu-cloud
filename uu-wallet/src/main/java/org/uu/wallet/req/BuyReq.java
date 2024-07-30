package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ApiModel(description = "买入下单请求参数")
public class BuyReq {

    /**
     * 代付订单号
     */
    @ApiModelProperty(value = "代付订单号")
    @NotBlank(message = "Sell order number cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Sell order number format is incorrect")
    private String platformOrder;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    @NotNull(message = "Order amount cannot be empty")
    @DecimalMin(value = "0.00", message = "Order amount format is incorrect")
    private BigDecimal amount;

    /**
     * kycId
     */
    @ApiModelProperty("kycId")
//    @NotNull(message = "kycId cannot be empty")
    private Long kycId;
}
