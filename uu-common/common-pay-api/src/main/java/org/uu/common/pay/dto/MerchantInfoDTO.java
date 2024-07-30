package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "商户信息")
public class MerchantInfoDTO implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名称")
    private String username;

    /**
     * 账号
     */
    @ApiModelProperty(value = "商家号")
    private String account;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户号 或者 appid")
    private String code;


    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 国家
     */
    @ApiModelProperty(value = "国家")
    private String country;


    /**
     * 谷歌身份验证密钥
     */
    @ApiModelProperty(value = "谷歌身份密钥")
    private String googlesecret;

    /**
     * 商家网址
     */

    @ApiModelProperty(value = "商家网站")
    private String website;

    /**
     * 登录次数
     */
    @ApiModelProperty(value = "登录次数")
    private Integer logins;

    /**
     * 支付费率
     */
    @ApiModelProperty(value = "代收费率")
    private BigDecimal payRate;

    /**
     * 代付费率
     */
    @ApiModelProperty(value = "代付费率")
    private BigDecimal transferRate;

    /**
     * 下发usdt地址
     */
    @ApiModelProperty(value = "下发usdt地址")
    private String usdtAddress;


    /**
     * 总下分次数: 统计商户自己下分次数
     */
    @ApiModelProperty(value = "下发次数")
    private Long transferDownCount;

    /**
     * 总下分金额: 统计商户自己提现金额
     */
    @ApiModelProperty(value = "下发金额")
    private BigDecimal transferDownAmount;


    /**
     * 开通时间
     */
    @ApiModelProperty(value = "开通时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * md5Key
     */
    @ApiModelProperty(value = "商户密钥")
    private String md5Key;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 总提现金额
     */
    @ApiModelProperty(value = "总付统计(总代付金额统计)")
    private BigDecimal totalWithdrawAmount;

    /**
     * 总充值金额
     */
    @ApiModelProperty(value = "总代收统计(总代收金额统计)")
    private BigDecimal totalPayAmount;


    @ApiModelProperty(value = "充值匹配过期时间")
    private Integer rechargeMatchExptime;
    @ApiModelProperty(value = "提现匹配过期时间")
    private Integer withdrawalMatchExptime;
    @ApiModelProperty(value = "确认过期时间")
    private Integer confimExptime;

    @ApiModelProperty(value = "商户类型")
    private String merchantType;


    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商户会员充值奖励")
    private BigDecimal rechargeReward;
    @ApiModelProperty(value = "商户会员提现奖励")
    private BigDecimal withdrawalRewards;

    @ApiModelProperty(value = "时区")
    private String timeZone;

    @ApiModelProperty(value = "上次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 登录IP
     */
    @ApiModelProperty(value = "登录IP")
    private String loginIp;


    @ApiModelProperty(value = "密码提示")
    private String passwordTips;

    @ApiModelProperty(value = "usdt汇率")
    private BigDecimal usdtRate;

    /**
     * 商户公钥
     */
    @ApiModelProperty(value = "商户公钥")
    private String merchantPublicKey;

    /**
     * 平台公钥
     */
    @ApiModelProperty(value = "平台公钥")
    private String platformPublicKey;


    @ApiModelProperty(value = "快捷金额")
    private String quickAmount;
    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "充值奖励比例")
    private BigDecimal rechargeRewardRatio;

    @ApiModelProperty(value = "法币交易中金额")
    private BigDecimal pendingBalance;

    @ApiModelProperty(value = "USDT交易中金额")
    private BigDecimal pendingUsdtBalance;

    @ApiModelProperty(value = "TRX交易金额")
    private BigDecimal pendingTrxBalance;

    @ApiModelProperty(value = "商户代收信息统计")
    private List<MerchantCollectStatistics> merchantCollectStatisticsList;


    @ApiModelProperty("USDT余额")
    private BigDecimal usdtBalance;


    /**
     * TRX余额
     */
    @ApiModelProperty("TRX余额")
    private BigDecimal trxBalance;


    /**
     * 商户代收、代付、下发信息统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchantCollectStatistics {
        @ApiModelProperty(value = "代收币种")
        private String currency;

        @ApiModelProperty(value = "累计代收订单数")
        private Long collectionOrdersNumber;

        @ApiModelProperty(value = "累计代收金额")
        private BigDecimal collectionAmountTotal;

        @ApiModelProperty(value = "代收费率")
        private String collectionRates;

        @ApiModelProperty(value = "累计代收费用")
        private BigDecimal collectionCostTotal;

        @ApiModelProperty(value = "累计代付订单数")
        private Long paymentOrdersNumber;

        @ApiModelProperty(value = "累计代付金额")
        private BigDecimal paymentAmountTotal;

        @ApiModelProperty(value = "代付费率")
        private String paymentRates;

        @ApiModelProperty(value = "累计代付费用")
        private BigDecimal paymentCostTotal;

        @ApiModelProperty(value = "下发次数")
        private Long issuedNumber;

        @ApiModelProperty(value = "下发金额")
        private BigDecimal issuedTotal;

    }
}