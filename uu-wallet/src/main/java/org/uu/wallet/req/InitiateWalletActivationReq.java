package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 激活钱包接口 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "激活钱包接口请求参数")
public class InitiateWalletActivationReq implements Serializable {


    /**
     * token
     */
    @NotBlank(message = "token cannot be empty")
    @ApiModelProperty("token")
    private String token;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    @NotNull(message = "mobileNumber cannot be empty")
    @Pattern(regexp = "^\\d{8,13}$", message = "mobileNumber format is incorrect")
    private String mobileNumber;

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
    @NotBlank(message = "paymentPassword can not be empty")
    @Pattern(regexp = "^\\d{4}$", message = "paymentPassword format is incorrect")
    private String paymentPassword;


    /**
     * 支付密码提示语
     */
    @ApiModelProperty(value = "支付密码提示语")
    @NotBlank(message = "Payment password prompt cannot be empty")
    @Pattern(regexp = "^.{0,10}$", message = "Please fill in no more than 10 characters")
    private String paymentPasswordHint;
}
