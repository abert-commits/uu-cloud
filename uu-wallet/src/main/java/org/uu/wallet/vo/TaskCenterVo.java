package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "任务中心页面返回数据")
public class TaskCenterVo implements Serializable {

    /**
     * 累计任务奖励金额
     */
    @ApiModelProperty(value = "累计任务奖励金额")
    private BigDecimal totalTaskRewards;


    /**
     * 新人任务列表
     */
    @ApiModelProperty(value = "新人任务列表")
    private List<MemberTaskVo> beginnerTasks;


    /**
     * 每日任务列表
     */
    @ApiModelProperty(value = "每日任务列表")
    private List<MemberTaskVo> dailyTaskList;


    /**
     * 领奖会员列表
     */
    @ApiModelProperty(value = "领奖会员列表")
    private List<PrizeWinnersVo> prizeWinners;
}