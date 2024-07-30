package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author lukas
 */
@Data
@ApiModel(description = "余额退回调用接口返回")
public class CashBackOrderApiDTO {
    @ApiModelProperty("会员id")
    private String orderNo;

    @ApiModelProperty("调用接口返回结果")
    private Boolean result;

    @ApiModelProperty("描述")
    private String remark;
}
