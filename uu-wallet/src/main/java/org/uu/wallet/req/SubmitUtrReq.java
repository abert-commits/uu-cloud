package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "提交UTR接口请求参数")
public class SubmitUtrReq {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "买入订单号")
    @NotBlank(message = "Buy order number cannot be empty")
    @Pattern(regexp = "^MR\\d{17}\\d{5}$", message = "The buy order number format is incorrect")
    private String platformOrder;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR (格式为12位长度纯数字的UTR)")
    @NotNull(message = "UTR cannot be empty")
    @Pattern(regexp = "^\\d{12}$", message = "UTR format is incorrect")
    private String utr;
}
