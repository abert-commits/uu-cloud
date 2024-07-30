package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代收订单月表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiPaymentOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private String dateTime;

    @ApiModelProperty(value = "买入订单金额")
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    @ApiModelProperty(value = "下单笔数")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "成功笔数")
    private Long successOrderNum = 0L;

    @ApiModelProperty(value = "买入成功率")
    private Double successRate;

    @ApiModelProperty(value = "申诉订单笔数")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "取消订单笔数")
    private Long cancelOrder = 0L;

    @ApiModelProperty(value = "取消支付订单笔数")
    private Long cancelPay = 0L;

    @ApiModelProperty(value = "平均完成时长")
    private Long averageFinishDuration = 0L;

    @ApiModelProperty(value = "买入奖励")
    private BigDecimal totalFee = BigDecimal.ZERO;












}