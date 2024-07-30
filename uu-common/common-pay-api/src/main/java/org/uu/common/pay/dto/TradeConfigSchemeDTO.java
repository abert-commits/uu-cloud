package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 交易配置方案表
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@Data
@ApiModel(description = "配置方案")
public class TradeConfigSchemeDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    private long id;

    /**
     * 方案名称
     */
    @ApiModelProperty("方案名称")
    private String schemeName;

    /**
     * 账号标签 1-激活钱包 2-实名认证
     */
    @ApiModelProperty("账号标签")
    private String schemeTag;

    /**
     * 最大买入金额
     */
    @ApiModelProperty("最大买入金额")
    private BigDecimal schemeMaxPurchaseAmount;

    /**
     * 最小买入金额
     */
    @ApiModelProperty("最小买入金额")
    private BigDecimal schemeMinPurchaseAmount;

    /**
     * 最大卖出金额
     */
    @ApiModelProperty("最大卖出金额")
    private BigDecimal schemeMaxSellAmount;

    /**
     * 最小卖出金额
     */
    @ApiModelProperty("最小卖出金额")
    private BigDecimal schemeMinSellAmount;

    /**
     * 同时卖出最多订单数
     */
    @ApiModelProperty("同时卖出最多订单数")
    private Integer schemeMaxSellOrderNum;

    /**
     * 卖出奖励比例
     */
    @ApiModelProperty("卖出奖励比例")
    private BigDecimal schemeSalesBonusProportion;

    /**
     * 卖出奖励金额日限制
     */
    @ApiModelProperty("卖出奖励金额日限制")
    private BigDecimal schemeSalesBonusAmountLimit;

    /**
     * 卖出奖励笔数日限制
     */
    @ApiModelProperty("卖出奖励笔数日限制")
    private Integer schemeSalesBonusNumLimit;

    /**
     * 确认超时时间
     */
    @ApiModelProperty("确认超时时间")
    private Integer schemeConfirmExpirationTime;

    /**
     * 卖出匹配时长(秒)
     */
    @ApiModelProperty("卖出匹配时长")
    private Integer schemeSellMatchingDuration;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;


    /**
     * 最后更新时间
     */
    @ApiModelProperty(value = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    private String updateBy;

}
