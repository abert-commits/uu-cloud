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
 * 代付订单月报
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_withdraw_order_month")
public class BiWithdrawOrderMonth implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    private String dateTime;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "实际金额")
    private BigDecimal actualMoney = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    @ApiModelProperty(value = "下单总笔数")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "成功笔数")
    private Long successOrderNum = 0L;

    /**
     * 总费用
     */
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalFee = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "上一次执行时间：22:05")
    private String lastMinute;


    @ApiModelProperty(value = "匹配超时订单笔数")
    private Long overTimeNum = 0L;

    @ApiModelProperty(value = "取消匹配订单数量")
    private Long cancelMatchNum = 0L;

    @ApiModelProperty(value = "申诉订单数量")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "继续匹配订单数量")
    private Long continueMatchNum = 0L;

    @ApiModelProperty(value = "匹配总时长")
    private Long matchDuration = 0L;

    @ApiModelProperty(value = "完成总时长")
    private Long finishDuration = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "平均匹配时长")
    private Long averageMatchDuration = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "平均完成时长")
    private Long averageFinishDuration = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "成功率")
    private Double successRate;

    @ApiModelProperty(value = "商户code")
    private String merchantCode;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入订单金额总额")
    private BigDecimal actualMoneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入奖励总额")
    private BigDecimal feeTotal;

}