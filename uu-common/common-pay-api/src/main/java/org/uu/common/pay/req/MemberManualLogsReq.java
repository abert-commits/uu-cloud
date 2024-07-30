package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author
 */
@Data
@ApiModel(description = "用户短信请求参数")
public class MemberManualLogsReq extends PageRequest {

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "操作人")
    private String createBy;

    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    private Integer opType;


    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private String endTime;

}