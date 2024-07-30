package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@ApiModel(description = "设置用户昵称请求参数")
public class UpdateNicknameReq {

    /**
     * 昵称
     */
    @ApiModelProperty(value = "用户昵称")
    @NotBlank(message = "User nickname cannot be empty")
    @Pattern(regexp = "^.{0,12}$", message = "Please fill in no more than 12 characters")
    private String nickname;
}