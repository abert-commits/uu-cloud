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
@ApiModel(description = "忘记支付密码接口请求参数")
public class ResetPaymentPasswordReq {

    /**
     * 验证码
     */
    @NotNull(message = "verification code must be filled")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Verification code error")
    private String verificationCode;

    /**
     * 支付密码
     */
    @ApiModelProperty(value = "支付密码")
    @NotBlank(message = "Payment password cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Please fill in the 6-digit pure numeric payment password")
    private String paymentPassword;

//    /**
//     * 支付密码提示语
//     */
//    @ApiModelProperty(value = "支付密码提示语")
//    @NotBlank(message = "Payment password prompt cannot be empty")
//    @Pattern(regexp = "^.{0,10}$", message = "Please fill in no more than 10 characters")
//    private String paymentPasswordHint;
}