package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "新手引导请求参数")
public class NewUserGuideReq implements Serializable {

    @ApiModelProperty("类型 1:买入引导 2:卖出引导")
    @NotNull(message = "type must be filled")
    private Integer type;


}