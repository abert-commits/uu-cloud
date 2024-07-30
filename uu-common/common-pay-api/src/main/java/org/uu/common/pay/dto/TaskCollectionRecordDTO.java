package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员短信
 *
 * @author
 */
@Data
@ApiModel(description = "会员领取任务记录返回")
public class TaskCollectionRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private String memberId;

    /**
     * 商户code
     */
    @ApiModelProperty("商户code")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 任务订单号
     */
    @ApiModelProperty("任务订单号")
    private String orderNo;

    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String taskName;

    /**
     * 任务id
     */
    @ApiModelProperty("任务id")
    private Integer taskId;

    /**
     * 任务类型
     */
    @ApiModelProperty("任务类型: 1-买入 2-卖出 3-签到 4-实名认证 5-新手任务")
    private Integer taskType;

    /**
     * 任务周期
     */
    @ApiModelProperty("任务周期：1:一次性任务 2:周期性-每天")
    private Integer taskCycle;

    /**
     * 奖励金额
     */
    @ApiModelProperty("奖励金额")
    private BigDecimal rewardAmount;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty("领取时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 完成时间
     */
    @ApiModelProperty("完成时间")
    private LocalDateTime completionTime;

    /**
     * 完成人数
     */
    @ApiModelProperty("完成人数")
    private Long completionNum = 0L;

    /**
     * 领奖人数
     */
    @ApiModelProperty("领奖人数")
    private Long recipientsNum = 0L;

    /**
     * 奖励总金额
     */
    @ApiModelProperty("奖励总金额")
    private BigDecimal totalReward = BigDecimal.ZERO;
}