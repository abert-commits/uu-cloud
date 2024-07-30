package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "货币配置请求参数")
public class MerchantCurrencyReq {

    private Long id;

    @ApiModelProperty(value = "币种符号")
    private String currencyCode;


    @ApiModelProperty(value = "币种名称")
    private String currencyName;
}
