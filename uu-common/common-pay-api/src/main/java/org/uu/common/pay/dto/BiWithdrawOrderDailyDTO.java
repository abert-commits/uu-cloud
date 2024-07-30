package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代付订单月报
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiWithdrawOrderDailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private String dateTime;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "卖出订单金额")
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 成功下单
     */
    @ApiModelProperty(value = "下单笔数")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "成功笔数")
    private Long successOrderNum = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "卖出成功率")
    private Double successRate;

    @ApiModelProperty(value = "申诉订单笔数")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "取消匹配订单笔数")
    private Long cancelMatchNum = 0L;

    @ApiModelProperty(value = "继续匹配订单笔数")
    private Long continueMatchNum = 0L;

    @ApiModelProperty(value = "平均完成时长")
    private Double avgDuration = 0.0;

    @ApiModelProperty(value = "卖出奖励")
    private BigDecimal totalFee = BigDecimal.ZERO;




}