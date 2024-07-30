package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现接口 返回数据1
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
public class WithdrawalApplyVo implements Serializable {


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
     * 会员id
     */
    private String memberId;


    /**
     * 订单金额
     */
    private BigDecimal amount;


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
}