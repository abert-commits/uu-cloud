package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "领取活动任务奖励请求参数")
public class ClaimTaskRewardReq {

    /**
     * 任务id
     */
    @ApiModelProperty(value = "任务id")
    @NotNull(message = "Task ID cannot be null.")
    @Min(value = 1, message = "Task ID must be a positive integer.")
    private Long taskId;

}
