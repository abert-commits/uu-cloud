package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;
@Data
@ApiModel(description = "系统日志请求参数")
public class SysLogReq  extends PageRequest {
    @ApiModelProperty(value = "请求Ip")
    private String ip;
    @ApiModelProperty(value = "请求方法")
    private String method;

    @ApiModelProperty(value = "路径")
    private String path;
    @ApiModelProperty(value = "模块")
    private String module;

    /**
     * 开始时间 时间戳(10位)
     */
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    /**
     * 结束时间 时间戳(10位)
     */
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "关键字")
    private String keyStr;
    @ApiModelProperty(value = "操作")
    private String content;

}
