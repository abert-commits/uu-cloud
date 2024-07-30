package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "币种对应的代收代付类型请求参数")
public class CurrencyPayTypeReq {

    @ApiModelProperty(value = "币种id")
    @NotNull(message = "currencyId cannot be null")
    private Long currencyId;

    @ApiModelProperty(value = "币种")
    private String currency;


    @ApiModelProperty(value = "1:代收 2:代付")
    @NotNull(message = "type cannot be null")
    private Integer type;

    @ApiModelProperty(value = "代收、代付的code:比如1:银行卡, 3:UPI, 5:银行卡和UPI")
    private String payType;

    @ApiModelProperty(value = "代收、代付的具体类型：例如upi、银行")
    private String payTypeName;
}
