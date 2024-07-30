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
public class BiPaymentEnOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty(value = "Date Time")
    private String dateTime;

    @ApiModelProperty(value = "Actual Money")
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    @ApiModelProperty(value = "Order Number")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "Success Order Number")
    private Long successOrderNum = 0L;

    @ApiModelProperty(value = "Success Rate")
    private Double successRate;

    @ApiModelProperty(value = "Appeal Number")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "Cancel Order")
    private Long cancelOrder = 0L;

    @ApiModelProperty(value = "Cancel Pay")
    private Long cancelPay = 0L;

    @ApiModelProperty(value = "Average Finish Duration")
    private Long averageFinishDuration = 0L;

    @ApiModelProperty(value = "Total Fee")
    private BigDecimal totalFee = BigDecimal.ZERO;












}