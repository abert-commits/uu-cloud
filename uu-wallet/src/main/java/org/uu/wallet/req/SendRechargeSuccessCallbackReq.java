package org.uu.wallet.req;


import lombok.Data;

import java.io.Serializable;

/**
 * 充值成功 异步通知商户 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class SendRechargeSuccessCallbackReq implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户订单号
     */
    private String merchantTradeNo;

    /**
     * 平台订单号
     */
    private String tradeNo;

    /**
     * 实际金额
     */
    private String amount;

    /**
     * 订单金额
     */
    private String orderAmount;

    /**
     * 交易状态
     */
    private String tradeStatus;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 签名
     */
    private String sign;

    /**
     * 币种
     */
    private String currency;
}
