package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 *  请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "收银台 取消支付接口 请求参数")
public class CancelPaymentReq implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID

    /**
     * token
     */
    @NotBlank(message = "token cannot be empty")
    @ApiModelProperty("订单token")
    private String token;
}
