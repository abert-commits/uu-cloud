package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商户代收订单月表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_pay_order_month")
public class BiMerchantPayOrderMonth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String dateTime;

    /**
     * 商户编码
     */
    private String merchantCode;

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
     * 平均完成时长
     */
    private Long finishDuration;


}