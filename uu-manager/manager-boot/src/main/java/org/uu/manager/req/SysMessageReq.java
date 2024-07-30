package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

/**
 * @author admin
 * @date 2024/5/6 13:55
 */
@Data
@ApiModel("站内信请求参数")
public class SysMessageReq extends PageRequest {

    @ApiModelProperty("消息类型 1-短信余额信息 2-未交易信息")
    private Integer messageType;

    @ApiModelProperty("消息状态 0-未读 1-已读")
    private Integer messageStatus;
}
