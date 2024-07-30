package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author
 */
@Data
@ApiModel(description = "会员操作请求参数")
public class MemberOperationLogsReq extends PageRequest {

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    private String userId;

    /**
     * 验证码类型
     */
    @ApiModelProperty(value = "操作模块：")
    private String opModule;

    /**
     * 发送账号
     */
    @ApiModelProperty(value = "登录IP")
    private String loginIp;

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