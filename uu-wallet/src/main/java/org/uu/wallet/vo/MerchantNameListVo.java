package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商户名称列表")
public class MerchantNameListVo {

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户编码")
    private String value;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String label;
}
