package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 充值接口 返回数据
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
public class DepositApplyVo implements Serializable {


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 支付地址
     */
    private String payUrl;

    /**
     * 订单token
     */
    private String token;

    /**
     * 平台订单号
     */
    private String tradeNo;

    /**
     * 商户订单号
     */
    private String merchantTradeNo;

    /**
     * 充值订单有效期
     */
    private Long orderValidityDuration;

    /**
     * 签名
     */
    private String sign;
}