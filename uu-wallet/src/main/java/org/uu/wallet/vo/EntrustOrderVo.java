package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 马上卖出/委托下单返回Vo
 */
@Data
public class EntrustOrderVo implements Serializable {

    /**
     * 连接中kyc数量
     */
    @ApiModelProperty(value = "链接中的UPI KYC")
    private Integer connectingKycUpiCount;

    /**
     * 连接中bank数量
     */
    @ApiModelProperty(value = "链接中的BANK KYC")
    private Integer connectingKycBankCount;

    /**
     * 是否自动卖单 1 是 0 否 默认1
     */
    @ApiModelProperty(value = "是否自动卖单 默认：1 自动卖单")
    private String isNoAutomaticSellOrder;

    /**
     * 委托状态: 1、委托成功 0、委托失败
     */
    private String delegationStatus;

    /**
     * 预计奖励
     */
    @ApiModelProperty("预计奖励-买入奖励比例")
    private BigDecimal estimatedReward;


    @ApiModelProperty(value = "INR买入iToken汇率")
    private BigDecimal inrBuyItoken;

    @ApiModelProperty(value = "usdt买入iToken汇率")
    private BigDecimal usdtBuyItoken;

    /**
     * 获取连接kyc的总数量
     */
    @ApiModelProperty("Kyc连接总数量")
    private Integer kycCollectionCount;

    @ApiModelProperty(value = "今日交易情况")
    private TodayTraction todayTraction;

    @Builder
    @Data
    public static class TodayTraction {
        /*
   会员账户现有ARB的数量
    */
        @ApiModelProperty("账户现有ARB的数量")
        private BigDecimal balance;

        /*
        总奖励
         */
        @ApiModelProperty("总奖励")
        private BigDecimal totalBonus;

        /*
        今日交易中(冻结金额)
         */
        @ApiModelProperty("交易中")
        private BigDecimal frozenAmount;

        /*
        今日交易金额
         */
        @ApiModelProperty("今日交易金额")
        private BigDecimal todayTradeAmount;

        /*
        今日奖励
         */
        @ApiModelProperty("今日奖励")
        private BigDecimal todayBonus;

        /*
    买入奖励比例
     */
        @ApiModelProperty("买入奖励比例")
        private BigDecimal buyBonusProportion;

        /*
        USDT汇率
         */
        @ApiModelProperty("USDT汇率")
        private BigDecimal usdtRate;

    }


}