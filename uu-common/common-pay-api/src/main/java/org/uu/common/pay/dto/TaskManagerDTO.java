package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 任务管理表
 * </p>
 *
 * @author 
 * @since 2024-03-18
 */
@Data
@ApiModel(description = "任务管理")
public class TaskManagerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 排序权重
     */
    @ApiModelProperty("排序权重")
    private Integer taskSort;

    /**
     * 任务类型: 1-买入任务, 2-卖出任务, 3-签到任务,4-实名认证任务 5-新手任务
     */
    @ApiModelProperty("任务类型: 1-买入任务, 2-卖出任务, 3-签到任务,4-实名认证任务 5-新手任务-买入引导 6-新手任务-卖出引导")
    private String taskType;

    /**
     * 任务周期：1-一次性 2-周期性-每天
     */
    @ApiModelProperty("任务周期：1-一次性 2-周期性-每天")
    private String taskCycle;

    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String taskTitle;

    /**
     * 任务副标题
     */
    @ApiModelProperty("任务副标题")
    private String taskSubTitle;

    /**
     * 任务icon
     */
    @ApiModelProperty("任务icon")
    private String taskIcon;

    /**
     * 跳转链接
     */
    @ApiModelProperty("跳转链接")
    private String taskJumpLink;

    /**
     * 任务目标类型 1-次数 2-金额
     */
    @ApiModelProperty("任务目标类型 1-次数 2-金额")
    private String taskTarget;

    /**
     * 目标数值
     */
    @ApiModelProperty("目标数值")
    private Integer taskTargetNum;

    /**
     * 任务奖励
     */
    @ApiModelProperty("任务奖励")
    private BigDecimal taskReward;

    /**
     * 任务状态 0-禁用 1-启用
     */
    @ApiModelProperty("任务状态 0-禁用 1-启用")
    private String taskStatus;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;


    /**
     * 最后更新时间
     */
    @ApiModelProperty(value = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    private String updateBy;
}
