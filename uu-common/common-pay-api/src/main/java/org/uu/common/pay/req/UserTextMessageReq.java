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
public class UserTextMessageReq extends PageRequest {

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    private String userId;

    /**
     * 验证码类型
     */
    @ApiModelProperty(value = "验证码类型：短信-SMS、邮箱-EMAIL")
    private String type;

    /**
     * 发送账号
     */
    @ApiModelProperty(value = "接收验证码账号")
    private String receiver;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "发送开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "发送结束时间")
    private String endTime;

}