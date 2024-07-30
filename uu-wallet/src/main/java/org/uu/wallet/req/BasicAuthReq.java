package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "获取BasicAuth请求参数")
public class BasicAuthReq {

    /**
     * 用户名
     */
    @NotBlank(message = "Username cannot be empty")
    @ApiModelProperty(value = "用户名")
    @Pattern(regexp = "^[a-zA-Z0-9._@-]{1,15}$", message = "Username format is incorrect")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "Password cannot be empty")
    @ApiModelProperty(value = "密码")
    @Pattern(regexp = "^[A-Za-z0-9!@#$%^&*()._-]{1,20}$", message = "Password format is incorrect")
    private String password;

}
