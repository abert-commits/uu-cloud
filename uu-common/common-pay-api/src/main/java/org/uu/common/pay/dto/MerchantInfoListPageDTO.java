package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "商户信息")
public class MerchantInfoListPageDTO implements Serializable {

    @ApiModelProperty(value = "主键")
    private Long id;
    /**
     * 商户名
     */

    @ApiModelProperty(value = "商户名称")
    private String username;
    /**
     * 状态
     */

    @ApiModelProperty(value = "状态 1正常代收0禁止代收2禁止代付3正常代付")
    private String status;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "appid或商户号")
    private String code;

    /**
     * 商家网址
     */
    @ApiModelProperty(value = "商家网址")
    private String website;

    @ApiModelProperty(value = "时区")
    private String timeZone;

    /**
     * 开通时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 支付费率
     */
    @ApiModelProperty(value = "代收费率")
    private String payRate;

    /**
     * 代付费率
     */
    @ApiModelProperty(value = "代付费率")
    private String transferRate;


    @ApiModelProperty(value = "商户类型 1内部商户 2 外部商户")
    private String merchantType;

    @ApiModelProperty(value = "代收状态 0禁止 1正常")
    private String rechargeStatus;

    @ApiModelProperty(value = "代付状态 0禁止 1正常")
    private String withdrawalStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "修改人")
    private String updateBy;

    @ApiModelProperty(value = "最大费用")
    private BigDecimal maxCost;
    @ApiModelProperty(value = "最少费用")
    private BigDecimal minCost;

    @ApiModelProperty(value = "充值匹配时间")
    private Integer rechargeMatchExptime;
    @ApiModelProperty(value = "提款匹配时间")
    private Integer withdrawalMatchExptime;
    @ApiModelProperty(value = "确认过期时间")
    private Integer confimExptime;
    @ApiModelProperty(value = "备注")
    private String remark;


    @ApiModelProperty(value = "商户会员充值奖励")
    private BigDecimal rechargeReward;
    @ApiModelProperty(value = "商户会员提现奖励")
    private BigDecimal withdrawalRewards;

    @ApiModelProperty(value = "会员总余额")
    private BigDecimal memberTotalBalance = BigDecimal.ZERO;

    @ApiModelProperty(value = "会员数量")
    private Long memberTotalNum = 0L;

    @ApiModelProperty(value = "API接口IP白名单, 多个IP以英文逗号, 分割")
    private String apiAllowedIps;

    @ApiModelProperty(value = "快捷金额")
    private String quickAmount;
    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "充值奖励比例")
    private BigDecimal rechargeRewardRatio;

    @ApiModelProperty(value = "风控标识 0-正常 1-操作超时 2-ip黑名单 3-余额过低")
    private String riskTag;
    /**
     * 汇率
     */
    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRates;
    /**
     * itoken数量
     */
    @ApiModelProperty(value = "支付金额")
    private Integer itokenNumber;

    /**
     * 启用的代收列表
     */
    private List<MerchantPayMentDTO> collectList;

    /**
     * 启用的代付列表
     */
    private List<MerchantPayMentDTO> paymentList;

    @ApiModelProperty(value = "币种表merchant_currency的主键id")
    private Long currencyId;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "国家")
    private String country;

    /**
     * USDT余额
     */
    @ApiModelProperty(value = "USDT余额")
    private BigDecimal usdtBalance;


    /**
     * TRX余额
     */
    @ApiModelProperty(value = "TRX余额")
    private BigDecimal trxBalance;


    @ApiModelProperty(value = "法币交易中金额")
    private BigDecimal pendingBalance;

    @ApiModelProperty(value = "USDT交易中金额")
    private BigDecimal pendingUsdtBalance;

    @ApiModelProperty(value = "TRX交易金额")
    private BigDecimal pendingTrxBalance;
}