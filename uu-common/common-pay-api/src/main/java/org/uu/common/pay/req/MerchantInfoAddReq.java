package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author
 */
@Data
@ApiModel(description = "商户请求参数说明")
public class MerchantInfoAddReq implements Serializable {


    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名称")
    private String username;


    @ApiModelProperty(value = "appid就是商户号")
    private String code;

    /**
     * 商家网址
     */
    @ApiModelProperty(value = "商家网站")
    private String website;

    @ApiModelProperty(value = "时区")
    private String timeZone;

    @ApiModelProperty(value = "国家")
    private String country;


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

    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxCost;
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minCost;

    @ApiModelProperty(value = "充值过期时间")
    private Integer rechargeMatchExptime;
    @ApiModelProperty(value = "出款过期时间")
    private Integer withdrawalMatchExptime;
    @ApiModelProperty(value = "确认过期时间")
    private Integer confimExptime;
    @ApiModelProperty(value = "备注")
    private String remark;


    @ApiModelProperty(value = "会员充值奖励")
    private BigDecimal rechargeReward;
    @ApiModelProperty(value = "会员提现奖励")
    private BigDecimal withdrawalRewards;
    @ApiModelProperty(value = "充值状态 0禁止 1正常")
    private String rechargeStatus;
    @ApiModelProperty(value = "出款状态 0禁止 1正常")
    private String withdrawalStatus;


    @ApiModelProperty(value = "API接口IP白名单, 多个IP以英文逗号, 分割")
    private String apiAllowedIps;

    @ApiModelProperty(value = "快捷金额")
    private String quickAmount;

    @ApiModelProperty(value = "商户图标")
    private String icon;

    @ApiModelProperty(value = "充值奖励比例")
    private BigDecimal rechargeRewardRatio;

    @ApiModelProperty(value = "货币")
    private Long currencyId;

    @ApiModelProperty(value = "货币名称")
    private String currency;
}