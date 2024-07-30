package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
public class WalletMemberInfoVo implements Serializable {


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 钱包激活状态, 取值说明: 1: 已激活, 0: 未激活
     */
    private String walletActivationStatus = "0";

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 提现奖励比例 %
     */
    private BigDecimal withdrawalRewardRatio;

    /**
     * 最小提现金额
     */
    private BigDecimal minimumWithdrawalAmount;

    /**
     * 最大提现金额
     */
    private BigDecimal maximumWithdrawalAmount;

    /**
     * 签名
     */
    private String sign;
}