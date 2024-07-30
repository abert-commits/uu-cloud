package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商户代付订单月报
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_withdraw_order_month")
public class BiMerchantWithdrawOrderMonth implements Serializable {

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
     * 匹配超时订单笔数
     */
    private Long overTimeNum;

    /**
     * 取消匹配订单数量
     */
    private Long cancelMatchNum;

    /**
     * 申诉订单数量
     */
    private Long appealNum;

    /**
     * 继续匹配订单数量
     */
    private Long continueMatchNum;

    /**
     * 匹配总时长
     */
    private Long matchDuration;

    /**
     * 完成总时长
     */
    private Long finishDuration;


}