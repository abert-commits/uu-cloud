package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "会员任务")
public class MemberTaskVo implements Serializable {

    /**
     * 任务id
     */
    @ApiModelProperty("任务id")
    private Long taskId;

    /**
     * 任务标题
     */
    @ApiModelProperty("任务标题")
    private String taskTitle;


    /**
     * 任务副标题
     */
    @ApiModelProperty("任务副标题")
    private Integer taskSubTitle;


    /**
     * 任务类型
     */
    @ApiModelProperty("任务类型, 取值说明: 1: 买入, 2: 卖出, 3: 签到, 4: 实名认证, 5: 新手引导")
    private String taskType;


    /**
     * 跳转链接
     */
    @ApiModelProperty("跳转链接")
    private String taskJumpLink;


    /**
     * 任务状态
     */
    @ApiModelProperty("任务状态 0: 未完成, 1: 待领取, 2：已完成 ")
    private Integer taskStatus;


    /**
     * 奖励金额
     */
    @ApiModelProperty("奖励金额")
    private BigDecimal taskReward;


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
     * 当前完成数值
     */
    @ApiModelProperty("当前完成数值")
    private String taskCurrentNum;

    /**
     * 任务icon
     */
    @ApiModelProperty("任务icon")
    private String taskIcon;
}