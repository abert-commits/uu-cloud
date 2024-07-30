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
public class InitiateAppWalletActivationReq implements Serializable {


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

}
