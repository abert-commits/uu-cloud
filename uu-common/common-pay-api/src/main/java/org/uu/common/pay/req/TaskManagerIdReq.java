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
public class TaskManagerIdReq extends PageRequest {

    @ApiModelProperty("主键")
    private Long id;

}
