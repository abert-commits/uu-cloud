package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel(description = "快捷买入下单请求参数")
public class QuickBuyMatchReq {

    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    @NotNull(message = "Order amount cannot be empty")
    @DecimalMin(value = "0.00", message = "Order amount format is incorrect")
    private BigDecimal amount;
}
