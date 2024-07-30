package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
public class CashBackVo implements Serializable {


    /**
     * 商户订单号
     */
    private String merchantOrder;


    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 订单金额
     */
    private String amount;

    /**
     * 订单状态 1-退回中 2-退回成功 3-退回失败
     */
    private String orderStatus;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户会员id
     */
    private String merchantMemberId;

    /**
     * 商户会员id
     */
    private String timestamp;

    /**
     * 签名
     */
    private String sign;
}