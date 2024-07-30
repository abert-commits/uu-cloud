package org.uu.wallet.req;


import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 提现接口 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class WithdrawalApplyReq implements Serializable {

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
     * 提现金额
     */
    @NotNull(message = "amount cannot be empty")
    @DecimalMin(value = "0.00", message = "amount format is incorrect")
    private String amount;


    /**
     * 渠道编码 取值说明: 1: 银行卡, 2: USDT, 3: UPI
     */
    @NotBlank(message = "channel cannot be empty")
    private String channel;


    /**
     * 时间戳
     */
    @NotBlank(message = "timestamp cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "timestamp format is incorrect")
    private String timestamp;


    /**
     * 异步回调地址
     */
    @NotBlank(message = "notifyUrl cannot be empty")
    private String notifyUrl;

    /**
     * 币种 INR
     */
    @NotBlank(message = "currency cannot be empty")
    private String currency;

    /**
     * 银行卡号
     */
//    @NotBlank(message = "bankCardNumber cannot be empty")
    private String bankCardNumber;

    /**
     * 银行名称
     */
//    @NotBlank(message = "bankName cannot be empty")
    private String bankName;

    /**
     * ifsc_code
     */
//    @NotBlank(message = "ifscCode cannot be empty")
    private String ifscCode;

    /**
     * 持卡人姓名
     */
//    @NotBlank(message = "bankCardOwner cannot be empty")
    private String bankCardOwner;


    /**
     * USDT充值地址
     */
    private String usdtAddr;

    /**
     * 签名
     */
    @NotBlank(message = "sign cannot be empty")
    private String sign;
}
