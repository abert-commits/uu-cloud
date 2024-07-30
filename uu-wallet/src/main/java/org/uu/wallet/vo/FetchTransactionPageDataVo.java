package org.uu.wallet.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 获取交易页面数据返回vo
 *
 * @author simon
 * @date 2024/07/11
 */
@Data
public class FetchTransactionPageDataVo implements Serializable {

    /**
     * iToken
     */
    @ApiModelProperty("iToken")
    private BigDecimal iToken;

    /**
     * 总奖励
     */
    @ApiModelProperty("总奖励")
    private BigDecimal totalBonus;

    /**
     * 交易中金额
     */
    @ApiModelProperty("交易中金额")
    private BigDecimal transactionAmount;

    /**
     * 今日交易成功金额
     */
    @ApiModelProperty("今日交易成功金额")
    private BigDecimal todayTransactionSuccessAmount;

    /**
     * 今日交易奖励
     */
    @ApiModelProperty("今日交易奖励")
    private BigDecimal todayTransactionReward;

    /**
     * 连接中的UPI KYC数量
     */
    @ApiModelProperty("连接中的UPI KYC数量")
    private Integer activeUpiKycCount;

    /**
     * 连接中的BANK KYC数量
     */
    @ApiModelProperty("连接中的BANK KYC数量")
    private Integer activeBankKycCount;

    /**
     * 当前用户已添加的kyc数量
     */
    @ApiModelProperty("用户kyc总数量")
    private Integer currentUserKycCount;

    /**
     * 买入奖励比例
     */
    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyBonusProportion;

    /**
     * 委托状态: 1、委托成功 0、委托失败
     */
    @ApiModelProperty("委托状态: 1: 委托中 0: 未开启委托")
    private Integer delegationStatus;

    @ApiModelProperty(value = "INR买入iToken汇率")
    private BigDecimal buyInrRatio;

    @ApiModelProperty(value = "usdt买入iToken汇率")
    private BigDecimal usdtBuyItoken;

    @ApiModelProperty("蚂蚁USDT最小充值金额")
    private BigDecimal minAntUsdtDepositAmount;

    @ApiModelProperty("蚂蚁USDT最大充值金额")
    private BigDecimal maxAntUsdtDepositAmount;

    /**
     * 买入状态: 买入是否被禁用
     */
    @ApiModelProperty("买入状态: 1: 启用 0: 禁用")
    private Integer buyStatus;

    /**
     * 卖出状态: 卖出是否被禁用
     */
    @ApiModelProperty("卖出状态: 1: 启用 0: 禁用")
    private Integer sellStatus;
}
