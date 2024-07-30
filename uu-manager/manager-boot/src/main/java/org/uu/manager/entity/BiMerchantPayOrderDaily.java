package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商户代收订单日表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_pay_order_daily")
public class BiMerchantPayOrderDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    private String dateTime;

    /**
     * 商户编码
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 订单金额
     */
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 实际金额
     */
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    private Long successOrderNum = 0L;

    /**
     * 总费用
     */
    private BigDecimal totalFee = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 上一次执行时间：22:05
     */
    private String lastMinute;

    /**
     * 取消支付订单数量
     */
    private Long cancelPay;

    /**
     * 取消订单数量
     */
    private Long cancelOrder;

    /**
     * 申诉订单数量
     */
    private Long appealNum;

    /**
     * 完成时长
     */
    private Long finishDuration;


    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    private Integer merchantType;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收交易笔数")
    private Long merchantPayTransNum;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收交易额")
    private BigDecimal merchantPayAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收手续费")
    private BigDecimal payFee;


}