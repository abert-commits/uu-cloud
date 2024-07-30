package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "一次验证码校验")
public class ValidateSmsCodeReq {

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
    @NotNull(message = "verification code must be filled")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Verification code error")
    private String verificationCode;
}