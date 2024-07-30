package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "手机号注册会员接口请求参数")
public class PhoneSignUpReq implements Serializable {

    private static final long serialVersionUID = 4942872509295760828L;
    /**
     * 手机号码
     */
    @NotNull(message = "Phone number can not be blank")
    @Pattern(regexp = "^\\d{8,13}$", message = "Mobile phone number format is incorrect")
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    private String mobileNumber;


    /**
     * 验证码
     */
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
}