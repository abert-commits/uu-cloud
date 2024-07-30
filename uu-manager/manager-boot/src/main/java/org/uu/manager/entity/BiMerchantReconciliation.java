package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商户对账报表
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_reconciliation")
public class BiMerchantReconciliation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private String dateTime;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户编码")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    @ApiModelProperty(value = "商户类型: 1.内部商户 2.外部商户")
    private String merchantType;

    /**
     * 商户金额
     */
    @ApiModelProperty(value = "商户金额")
    private BigDecimal merchantBalance = BigDecimal.ZERO;

    /**
     * 商户代收金额
     */
    @ApiModelProperty(value = "商户代收金额")
    private BigDecimal payMoney = BigDecimal.ZERO;

    /**
     * 商户代付金额
     */
    @ApiModelProperty(value = "商户代付金额")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;

    /**
     * 商户代收费用
     */
    @ApiModelProperty(value = "商户代收费用")
    private BigDecimal payFee = BigDecimal.ZERO;

    /**
     * 商户代付费用
     */
    @ApiModelProperty(value = "商户代付费用")
    private BigDecimal withdrawFee = BigDecimal.ZERO;

    /**
     * 商户上分金额
     */
    @ApiModelProperty(value = "商户上分金额")
    private BigDecimal merchantUp = BigDecimal.ZERO;

    /**
     * 商户下分金额
     */
    @ApiModelProperty(value = "商户下分金额")
    private BigDecimal merchantDown = BigDecimal.ZERO;

    /**
     * 商户账目偏差
     */
    @ApiModelProperty(value = "商户账目偏差")
    private BigDecimal merchantDiff = BigDecimal.ZERO;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private BigDecimal balanceTotal;

    @TableField(exist = false)
    private BigDecimal payMoneyTotal;

    @TableField(exist = false)
    private BigDecimal withdrawMoneyTotal;

    @TableField(exist = false)
    private BigDecimal payFeeTotal;

    @TableField(exist = false)
    private BigDecimal withdrawFeeTotal;

    @TableField(exist = false)
    private BigDecimal merchantUpTotal;

    @TableField(exist = false)
    private BigDecimal merchantDownTotal;

    @TableField(exist = false)
    private BigDecimal merchantDiffTotal;


}
