package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "奖励明细分页查询请求参数")
public class TaskCollectionRecordPageReq extends PageRequest {

    /**
     * 奖励类型
     */
    @ApiModelProperty("奖励类型，取值说明： 1: 买入, 2: 卖出, 3: 签到, 4: 实名认证, 5: 新手引导")
    @Pattern(regexp = "^\\d+$", message = "Invalid task type format")
    private String taskType;

    /**
     * 查询起始时间 (格式: YYYY-MM-DD)
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid query start time format")
    @ApiModelProperty(value = "查询起始时间 (格式: YYYY-MM-DD)")
    private String startDate;

    /**
     * 查询结束时间 (格式: YYYY-MM-DD)
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid query end time format")
    @ApiModelProperty(value = "查询结束时间 (格式: YYYY-MM-DD)")
    private String endDate;

}
