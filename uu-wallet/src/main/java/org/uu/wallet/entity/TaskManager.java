package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任务管理表
 * </p>
 *
 * @author 
 * @since 2024-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_manager")
public class TaskManager extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 排序权重
     */
    private Integer taskSort;

    /**
     * 任务类型: 1-买入任务, 2-卖出任务, 3-签到任务,4-实名认证任务 5-新手任务
     */
    private String taskType;

    /**
     * 任务周期：1-一次性 2-周期性-每天
     */
    private String taskCycle;

    /**
     * 任务名称
     */
    private String taskTitle;

    /**
     * 任务副标题
     */
    private String taskSubTitle;

    /**
     * 任务icon
     */
    private String taskIcon;

    /**
     * 跳转链接
     */
    private String taskJumpLink;

    /**
     * 任务目标类型 1-次数 2-金额
     */
    private String taskTarget;

    /**
     * 目标数值
     */
    private Integer taskTargetNum;

    /**
     * 任务奖励
     */
    private BigDecimal taskReward;

    /**
     * 任务状态 0-禁用 1-启用
     */
    private String taskStatus;

    /**
     * 删除 0未删除 1 已删除
     */
    private Integer deleted;

}
