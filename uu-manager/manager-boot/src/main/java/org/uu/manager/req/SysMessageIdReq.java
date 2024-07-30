package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @date 2024/5/6 14:08
 */
@Data
@ApiModel("站内信请求参数")
public class SysMessageIdReq {

    @ApiModelProperty(value = "主键")
    private Long id;
}
