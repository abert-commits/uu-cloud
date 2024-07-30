package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 连接 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "发送kyc验证码")
public class KycSendOtpReq implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @NotNull(message = "phoneNumber cannot be null")
    @Positive(message = "phoneNumber must be greater than 0")
    @ApiModelProperty(value = "phoneNumber")
    private String phoneNumber;


    /**
     * token
     */
    @NotBlank(message = "bankCode cannot be empty")
    @ApiModelProperty("bankCode")
    private String bankCode;

}
