package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@ApiModel(description = "绑定邮箱接口请求参数")
public class BindEmailReq {

    /**
     * 邮箱账号
     */
    @NotNull(message = "Email account cannot be empty")
    @Pattern(regexp = "^(.+)@(.+)$", message = "The email account format is incorrect")
    @ApiModelProperty(value = "邮箱账号 (格式为标准邮箱格式 示例: asd@123456.com)")
    private String emailAccount;

    /**
     * 验证码
     */
    @NotNull(message = "verification code must be filled")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Verification code error")
    private String verificationCode;
}