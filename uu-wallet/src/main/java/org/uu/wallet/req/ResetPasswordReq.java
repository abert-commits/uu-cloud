package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@ApiModel(description = "忘记密码接口请求参数")
public class ResetPasswordReq {

    /**
     * 会员账号
     */
    @NotBlank(message = "Member account cannot be empty")
    @ApiModelProperty(value = "会员账号")
    @Pattern(regexp = "^[a-zA-Z0-9._@-]{5,15}$", message = "Member account format is incorrect")
    private String memberAccount;

    /**
     * 验证码
     */
    @NotNull(message = "verification code must be filled")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Verification code error")
    private String verificationCode;

    /**
     * 登录密码
     */
    @NotNull(message = "password can not be blank")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,32}$", message = "Please enter a password containing letters and numbers between 8-32")
    @ApiModelProperty(value = "登录密码 (格式为8-32之间包含字母和数字的密码)")
    private String password;
}