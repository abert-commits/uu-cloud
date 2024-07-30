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
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 买入日报表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_payment_order_daily")
public class BiPaymentOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    @ApiModelProperty(value = "时间")
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
    @ApiModelProperty(value = "奖励")
    private BigDecimal totalFee = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "上一次执行时间：22:05")
    private String lastMinute;

    @ApiModelProperty(value = "取消支付订单数量")
    private Long cancelPay = 0L;

    @ApiModelProperty(value = "取消订单数量")
    private Long cancelOrder = 0L;

    @ApiModelProperty(value = "申诉订单数量")
    private Long appealNum = 0L;

    @ApiModelProperty(value = "完成时长")
    private Long finishDuration = 0L;

    @ApiModelProperty(value = "支付超时数量")
    private Long payOverTime = 0L;

    @ApiModelProperty(value = "确认超时数量")
    private Long confirmOverTime = 0L;

    @ApiModelProperty(value = "申诉成功数量")
    private Long appealSuccess = 0L;

    @ApiModelProperty(value = "申诉失败数量")
    private Long appealFail = 0L;

    @ApiModelProperty(value = "金额错误数量")
    private Long amountError = 0L;

    @ApiModelProperty(value = "金额错误数量")
    private Long cancel = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "平均完成时长")
    private Long averageFinishDuration = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "成功率")
    private Double successRate;

    @ApiModelProperty(value = "商户code")
    private String merchantCode;


    @TableField(exist = false)
    @ApiModelProperty(value = "订单交易额")
    private BigDecimal memberPayAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单交易数量")
    private Long memberPayTransNum;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段取消支付")
    private Long cancelPayTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-支付超时")
    private Long payOverTimeTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-确认超时数量")
    private Long confirmOverTimeTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-申诉成功数量")
    private Long appealSuccessTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-申诉失败数量")
    private Long appealFailTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-金额错误")
    private Long amountErrorTotal = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-已取消")
    private Long cancelOrderTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计字段-成功笔数")
    private Long successOrderNumTotal = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "总订单数")
    private Long orderNumTotal = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入订单金额总额")
    private BigDecimal actualMoneyTotal;


    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-总订单金额")
    private BigDecimal moneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-总奖励")
    private BigDecimal buyBonusTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入奖励总额")
    private BigDecimal feeTotal;

    @ApiModelProperty(value = "买入成功订单占比")
    private BigDecimal proportionSuccessOrders = BigDecimal.ZERO;

    @ApiModelProperty(value = "买入成功金额占比")
    private BigDecimal proportionSuccessAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "买入奖励")
    private BigDecimal buyBonus = BigDecimal.ZERO;
}