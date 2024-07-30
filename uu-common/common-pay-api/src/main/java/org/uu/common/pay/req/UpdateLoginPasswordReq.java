package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("修改登录密码请求实体类")
public class UpdateLoginPasswordReq implements Serializable {
    private static final long serialVersionUID = -7375526424309017653L;

    @ApiModelProperty(value = "修改类型 1-充值密码 2-修改密码", required = true)
    @NotNull(message = "Please specify the type of current update")
    @Min(value = 1, message = "The minimum value of updateType is 1")
    @Max(value = 2, message = "The maximum value of updateType is 2")
    private Integer updateType;

    @ApiModelProperty(value = "原密码")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true)
    @NotEmpty(message = "Please specify the newPassword of current update")
    private String newPassword;

    @ApiModelProperty(value = "手机号", required = true)
    @Pattern(regexp = "^\\d{8,13}$", message = "Mobile phone number format is incorrect")
    @NotEmpty(message = "Please specify the phoneNumber of current update")
    private String phoneNumber;

    @ApiModelProperty(value = "验证码", required = true)
    @NotEmpty(message = "Please specify the verificationCode of current update")
    @Pattern(regexp = "\\d{6}", message = "Invalid format for verification code")
    private String verificationCode;
}
