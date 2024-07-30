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
@ApiModel(description = "邮箱注册会员接口请求参数")
public class EmailSignUpReq {

    /**
     * 验证码
     */
    @NotNull(message = "Verification code cannot be empty")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Invalid format for verification code")
    private String verificationCode;

    /**
     * 登录密码
     */
    @NotNull(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,32}$", message = "Please enter a password between 8-32 characters long, including letters and numbers")
    @ApiModelProperty(value = "登录密码 (格式为8-32之间包含字母和数字的密码)")
    private String password;

    /**
     * 上级邀请码
     */
    @ApiModelProperty(value = "上级邀请码 (格式为长度在4-20之间的字母或数字)")
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "referrerCode format for invitation code")
    private String referrerCode;

    /**
     * 邮箱账号
     */
    @NotNull(message = "Email account cannot be empty")
    @Pattern(regexp = "^(.+)@(.+)$", message = "The email account format is incorrect")
    @ApiModelProperty(value = "邮箱账号 格式为标准邮箱格式 示例: asd@123456.com")
    private String emailAccount;
}