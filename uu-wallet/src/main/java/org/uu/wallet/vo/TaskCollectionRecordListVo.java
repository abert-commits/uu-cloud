package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "前台-会员领取任务记录分页查询列表")
public class TaskCollectionRecordListVo implements Serializable {

    /**
     * 任务标题
     */
    @ApiModelProperty("任务标题")
    private String taskName;

    /**
     * 奖励类型
     */
    @ApiModelProperty("奖励类型，取值说明： 1: 买入, 2: 卖出, 3: 签到, 4: 实名认证, 5: 新手引导")
    private Integer taskType;


    /**
     * 奖励金额
     */
    @ApiModelProperty("奖励金额")
    private BigDecimal rewardAmount;

    /**
     * 奖励领取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "奖励领取时间")
    private LocalDateTime createTime;


}