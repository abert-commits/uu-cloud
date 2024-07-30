package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lukas
 */
@Data
@ApiModel(description = "余额退回返回")
public class CashBackOrderProcessDTO {

    @ApiModelProperty("会员id")
    private Long memberId;

    @ApiModelProperty("加入队列结果")
    private Boolean result;

    @ApiModelProperty("描述")
    private String remark;
}
