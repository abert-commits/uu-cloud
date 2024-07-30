package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代付订单月报
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiWithdrawEnOrderDailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 日期
     */
    @ApiModelProperty(value = "Date Time")
    private String dateTime;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "Actual Money")
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 成功下单
     */
    @ApiModelProperty(value = "Order Number")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "Success Order Number")
    private Long successOrderNum = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "Success Rate")
    private Double successRate;

    @ApiModelProperty(value = "Appeal Number")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "Cancel Match Number")
    private Long cancelMatchNum = 0L;

    @ApiModelProperty(value = "Continue Match Number")
    private Long continueMatchNum = 0L;

    @ApiModelProperty(value = "Over Time Number")
    private Long overTimeNum = 0L;

    @ApiModelProperty(value = "Total Fee")
    private BigDecimal totalFee = BigDecimal.ZERO;




}