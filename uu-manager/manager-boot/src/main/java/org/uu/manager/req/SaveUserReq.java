package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Data
@ApiModel(description = "保存用户请求参数")
public class SaveUserReq {

    private Long id;

    @ApiModelProperty(value = "用户名")
    @NotBlank(message = "username 不能为空")
    private String username;

    @ApiModelProperty(value = "昵称")
    @NotBlank(message = "nickname 不能为空")
    private String nickname;

    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "mobile 不能为空")
    private String mobile;

    @ApiModelProperty(value = "性别")
    private Integer gender;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "状态")
    @NotNull(message = "状态 不能为空")
    private Integer status;

    @ApiModelProperty(value = "roleIds")
    @Size(min = 1, message = "roleIds 不能为空")
    private List<Long> roleIds;

}
