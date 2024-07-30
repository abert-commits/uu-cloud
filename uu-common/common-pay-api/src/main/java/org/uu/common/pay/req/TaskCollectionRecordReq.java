package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author
 */
@Data
@ApiModel(description = "会员领取任务记录参数")
public class TaskCollectionRecordReq extends PageRequest {

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private String userId;

    /**
     * 所属商户名称
     */
    @ApiModelProperty(value = "所属商户名称")
    private String merchantName;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    /**
     * 任务类型
     */
    @ApiModelProperty(value = "任务类型")
    private String taskType;

    /**
     * 任务周期
     */
    @ApiModelProperty(value = "任务周期")
    private String frequency;


    /**
     * 领取开始时间
     */
    @ApiModelProperty(value = "领取开始时间")
    private String receiveStartTime;

    /**
     * 领取结束时间
     */
    @ApiModelProperty(value = "领取结束时间")
    private String receiveEndTime;


    /**
     * 领取开始时间
     */
    @ApiModelProperty(value = "完成开始时间")
    private String completeStartTime;

    /**
     * 领取结束时间
     */
    @ApiModelProperty(value = "完成结束时间")
    private String completeEndTime;

}