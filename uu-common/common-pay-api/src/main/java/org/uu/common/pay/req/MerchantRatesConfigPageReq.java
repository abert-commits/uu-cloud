package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

/**
 * @author
 */
@Data
@ApiModel(description = "商户对应的代收、代付费率设置记录请求")
public class MerchantRatesConfigPageReq extends PageRequest {

    @ApiModelProperty("商户会员编码")
    private String merchantCode;

    @ApiModelProperty("1:代收 2:代付")
    private Integer type;


}