package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@ApiModel(description = "买入列表请求参数")
public class BuyListReq {

    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    @DecimalMin(value = "0.00", message = "Minimum amount format is incorrect")
    private BigDecimal minimumAmount = new BigDecimal("0.00");// TODO 现写死 后面从配置取

    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    @DecimalMin(value = "0.00", message = "Maximum amount format is incorrect")
    private BigDecimal maximumAmount = new BigDecimal("1000000.00");// TODO 现写死 后面从配置取

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;
}
