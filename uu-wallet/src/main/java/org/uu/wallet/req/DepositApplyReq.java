package org.uu.wallet.req;


import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 充值接口 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class DepositApplyReq implements Serializable {

    /**
     * 商户号
     */
    @NotBlank(message = "merchantCode cannot be empty")
    private String merchantCode;


    /**
     * 会员id
     */
    @NotBlank(message = "memberId cannot be empty")
    private String memberId;


    /**
     * 商户订单号
     */
    @NotBlank(message = "merchantTradeNo cannot be empty")
    private String merchantTradeNo;


    /**
     * 充值金额
     */
    @NotNull(message = "amount cannot be empty")
    @DecimalMin(value = "0.00", message = "amount format is incorrect")
    private String amount;


    /**
     * 渠道编码
     */
    @NotBlank(message = "channel cannot be empty")
    private String channel;


    /**
     * 异步回调地址
     */
    @NotBlank(message = "notifyUrl cannot be empty")
    private String notifyUrl;


    /**
     * 时间戳
     */
    @NotBlank(message = "timestamp cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "timestamp format is incorrect")
    private String timestamp;

    /**
     * 币种 INR
     */
    @NotBlank(message = "currency cannot be empty")
    private String currency;


    /**
     * 同步回调地址
     */
    @NotBlank(message = "syncNotifyAddress cannot be empty")
    private String syncNotifyAddress;


    /**
     * 签名
     */
    @NotBlank(message = "sign cannot be empty")
    private String sign;

}
