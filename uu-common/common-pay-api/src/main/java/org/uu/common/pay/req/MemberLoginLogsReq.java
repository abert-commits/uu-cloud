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
public class MemberLoginLogsReq extends PageRequest {

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    private String userId;

    /**
     * 登录类型
     */
    @ApiModelProperty(value = "登录类型：1:前台模式登录, 2:商户模式登录")
    private String type;

    /**
     * 会员账号
     */
    @ApiModelProperty(value = "会员账号")
    private String memberAccount;


    /**
     * 登录IP
     */
    @ApiModelProperty(value = "登录IP")
    private String loginIp;


    /**
     * 开始时间
     */
    @ApiModelProperty(value = "登录开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "登录结束时间")
    private String endTime;

}