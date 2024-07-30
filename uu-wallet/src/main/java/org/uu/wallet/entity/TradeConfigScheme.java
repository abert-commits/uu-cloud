package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 交易配置方案表
 * </p>
 *
 * @author 
 * @since 2024-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_config_scheme")
public class TradeConfigScheme extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 方案名称
     */
    private String schemeName;

    /**
     * 账号标签 1-激活钱包 2-实名认证
     */
    private String schemeTag;

    /**
     * 最大买入金额
     */
    private BigDecimal schemeMaxPurchaseAmount;

    /**
     * 最小买入金额
     */
    private BigDecimal schemeMinPurchaseAmount;

    /**
     * 最大卖出金额
     */
    private BigDecimal schemeMaxSellAmount;

    /**
     * 最小卖出金额
     */
    private BigDecimal schemeMinSellAmount;

    /**
     * 同时卖出最多订单数
     */
    private Integer schemeMaxSellOrderNum;

    /**
     * 卖出奖励比例
     */
    private BigDecimal schemeSalesBonusProportion;

    /**
     * 卖出奖励金额日限制
     */
    private BigDecimal schemeSalesBonusAmountLimit;

    /**
     * 卖出奖励笔数日限制
     */
    private Integer schemeSalesBonusNumLimit;

    /**
     * 确认超时时间
     */
    private Integer schemeConfirmExpirationTime;

    /**
     * 卖出匹配时长
     */
    private Integer schemeSellMatchingDuration;
}
