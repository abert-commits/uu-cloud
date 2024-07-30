package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会员任务状态表, 记录会员完成任务和领取奖励的状态
 * </p>
 *
 * @author 
 * @since 2024-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_task_status")
public class MemberTaskStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务完成状态（0未完成，1已完成）
     */
    private Integer completionStatus;

    /**
     * 奖励领取状态（0未领取，1已领取）
     */
    private Integer rewardClaimed;

    /**
     * 任务完成日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;

    /**
     * 奖励领取日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate claimDate;


    /**
     * 任务类型 1: 买入, 2: 卖出, 3: 签到, 4: 实名认证, 5: 新手引导
     */
    private Integer taskType;


    /**
     * 任务订单号
     */
    private String orderNo;


    /**
     * 任务完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     *  任务周期 1:一次性任务 2:周期性-每天
     */
    private Integer taskCycle;
}
