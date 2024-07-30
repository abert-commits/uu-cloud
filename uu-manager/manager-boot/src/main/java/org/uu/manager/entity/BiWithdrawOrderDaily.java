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
 * 代付订单日报表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_withdraw_order_daily")
public class BiWithdrawOrderDaily implements Serializable {

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
    @ApiModelProperty(value = "奖励")
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

    @TableField(exist = false)
    @ApiModelProperty(value = "平均匹配时长")
    private Long averageMatchDuration = 0L;


    @ApiModelProperty(value = "完成总时长")
    private Long finishDuration = 0L;

    @ApiModelProperty(value = "确认超时数量")
    private Long confirmOverTime = 0L;

    @ApiModelProperty(value = "申诉成功数量")
    private Long appealSuccess = 0L;

    @ApiModelProperty(value = "申诉失败数量")
    private Long appealFail = 0L;

    @ApiModelProperty(value = "金额错误数量")
    private Long amountError = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "平均完成时长")
    private Long averageFinishDuration = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "成功率")
    private Double successRate;

    @ApiModelProperty(value = "商户code")
    private String merchantCode;

    @ApiModelProperty(value = "已取消数量")
    private Long cancel = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单交易额")
    private BigDecimal memberWithdrawAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单交易数量")
    private Long memberWithdrawTransNum;



    @TableField(exist = false)
    @ApiModelProperty(value = "统计-匹配超时订单")
    private Long overTimeNumTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-确认超时订单")
    private Long confirmOverTimeTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-申诉成功订单")
    private Long appealSuccessTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-申诉失败订单")
    private Long appealFailTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-金额错误订单")
    private Long amountErrorTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-取消订单数量")
    private Long cancelOrderTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "统计-成功订单数量")
    private Long successOrderNumTotal;


    @TableField(exist = false)
    @ApiModelProperty(value = "统计-订单数量")
    private Long orderNumTotal;

    @ApiModelProperty(value = "统计-自动卖出人数总计")
    @TableField(exist = false)
    private Long automaticSellingNumberTotal;

    @ApiModelProperty(value = "统计-订单金额总计")
    @TableField(exist = false)
    private BigDecimal moneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "卖出奖励总计")
    private BigDecimal sellBonusTotal;


    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入订单金额总额")
    private BigDecimal actualMoneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "页面总计字段-买入奖励总额")
    private BigDecimal feeTotal;

    @ApiModelProperty(value = "成功卖出订单占比")
    private BigDecimal proportionSuccessSellOrders = BigDecimal.ZERO;

    @ApiModelProperty(value = "成功卖出金额占比")
    private BigDecimal proportionSuccessSellAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "卖出奖励")
    private BigDecimal sellBonus = BigDecimal.ZERO;

    @ApiModelProperty(value = "自动卖出人数")
    private Integer automaticSellingNumber = 0;
}