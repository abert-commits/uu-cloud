package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description = "匹配池列表返回")
public class MatchPoolListPageDTO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;


    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 最小限额
     */
    @ApiModelProperty("最小限额")
    private BigDecimal minimumAmount;

    /**
     * 已匹配订单数
     */
    @ApiModelProperty("已匹配订单数")
    private Integer orderMatchCount;

    /**
     * 进行中订单数
     */
    @ApiModelProperty("进行中订单数")
    private Integer inProgressOrderCount;

    /**
     * 已完成订单数
     */
    @ApiModelProperty("已完成订单数")
    private Integer completedOrderCount;

    /**
     * 已卖出金额
     */
    @ApiModelProperty("已卖出金额")
    private BigDecimal soldAmount;

    /**
     * 剩余金额
     */
    @ApiModelProperty("剩余金额")
    private BigDecimal remainingAmount;

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态  1: 匹配中,  2: 匹配超时,  7: 已完成,  8: 已取消,  14: 进行中")
    private String orderStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("提交匹配时间")
    private LocalDateTime createTime;




}