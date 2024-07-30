package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 不传递邀请码则创建顶级会员
 * 邀请码对应的用户类型需要与传入的用户类型一致
 */
@Data
@ApiModel(description = "创建会员请求参数")
public class MemberInfoReq implements Serializable {

    private static final long serialVersionUID = -2934016826944593733L;

    @NotBlank(message = "账号不能为空")
    @ApiModelProperty(value = "会员账号")
    private String memberAccount;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "登录密码")
    private String password;

    @NotEmpty(message = "会员类型不能为空")
    @ApiModelProperty(value = "会员类型")
    private String memberType;

    @ApiModelProperty(value = "备注")
    private String remark;


    @ApiModelProperty(value = "上级邀请码")
    private String inviteCode;
}