package org.uu.wallet.req;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 查询提现订单 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class WithdrawalQueryReq implements Serializable {

    /**
     * 商户号
     */
    @NotBlank(message = "merchantCode can not be empty")
    private String merchantCode;

    /**
     * 商户订单号
     */
    @NotBlank(message = "merchantTradeNo can not be empty")
    private String merchantTradeNo;

    /**
     * 签名
     */
    @NotBlank(message = "sign can not be empty")
    private String sign;
}
