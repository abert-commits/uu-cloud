package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@ApiModel(description = "修改支付密码请求参数")
public class UpdatePaymentPasswordReq {

    /**
     * 旧支付密码
     */
    @ApiModelProperty(value = "旧支付密码")
    @NotBlank(message = "Payment password cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Please fill in the 6-digit pure numeric payment password")
    private String oldPaymentPassword;

    /**
     * 新支付密码
     */
    @ApiModelProperty(value = "新支付密码")
    @NotBlank(message = "New payment password cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Please fill in the 6-digit pure numeric payment password")
    private String newPaymentPassword;

//    /**
//     * 支付密码提示语
//     */
//    @ApiModelProperty(value = "支付密码提示语")
//    @NotBlank(message = "Payment password prompt cannot be empty")
//    @Pattern(regexp = "^.{0,10}$", message = "Please fill in no more than 10 characters")
//    private String paymentPasswordHint;
}