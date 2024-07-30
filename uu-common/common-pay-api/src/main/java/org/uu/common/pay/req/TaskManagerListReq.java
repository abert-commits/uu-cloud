package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

/**
 * @author admin
 * @date 2024/3/18 11:57
 */
@Data
@ApiModel(description = "任务信息")
public class TaskManagerListReq extends PageRequest {

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务类型")
    private String taskType;

    @ApiModelProperty(value = "任务周期")
    private String taskCycle;

    @ApiModelProperty(value = "任务目标")
    private String taskTarget;

    @ApiModelProperty(value = "任务状态")
    private String taskStatus;
}
