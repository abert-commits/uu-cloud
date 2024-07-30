package org.uu.common.pay.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "蚂蚁信息注册请求实体")
public class AntInfoRegisterRequestVO implements Serializable {
    private static final long serialVersionUID = -2472984863734678056L;

    @NotNull(message = "Phone number can not be blank")
    @Pattern(regexp = "^\\d{8,13}$", message = "Mobile phone number format is incorrect")
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    private String phoneNumber;

    @NotNull(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,32}$", message = "Please enter a password between 8-32 characters long, including letters and numbers")
    @ApiModelProperty(value = "登录密码 (格式为8-32之间包含字母和数字的密码)")
    private String password;

    @ApiModelProperty(value = "确认密码(若传递则必须与登录密码保持一致)")
    private String confirmPassword;

    @NotNull(message = "Invitation code can not be blank")
    @ApiModelProperty(value = "上级邀请码 (格式为长度在4-20之间的字母或数字)")
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "referrerCode format for invitation code")
    private String inviteCode;

    @NotNull(message = "验证码不能为空")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Invalid format for verification code")
    private String verificationCode;
}
