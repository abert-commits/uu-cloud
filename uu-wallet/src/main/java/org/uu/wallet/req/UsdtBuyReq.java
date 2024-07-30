package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel(description = "USDT买入下单请求参数")
public class UsdtBuyReq {

    /**
     * USDT数量
     */
    @ApiModelProperty(value = "USDT数量")
    @NotNull(message = "USDT quantity cannot be empty")
    @DecimalMin(value = "0.00", message = "USDT quantity format is incorrect")
    private BigDecimal usdtAmount;

}
