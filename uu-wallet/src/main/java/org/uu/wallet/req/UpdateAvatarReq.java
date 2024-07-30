package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author
 */
@Data
@ApiModel(description = "设置用户头像请求参数")
public class UpdateAvatarReq {

    /**
     * 头像
     */
    @ApiModelProperty(value = "用户头像")
    @NotNull(message = "User avatar cannot be empty")
    @Min(value = 0, message = "User avatar parameter format error")
    private Integer avatar;
}