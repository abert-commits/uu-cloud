package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "实名认证接口请求参数")
public class IdenAuthenticationReq {

    /**
     * 真实姓名
     */
    @NotNull(message = "Do not leave blank for real name")
    @Pattern(regexp = "^[a-zA-Z]+(?:[\\s.][a-zA-Z]+)*$", message = "Real name format is incorrect")
    @ApiModelProperty(value = "真实姓名 (格式为印度人真实姓名格式 示例: Priya)")
    private String realName;

    /**
     * 证件号
     */
    @NotNull(message = "ID number cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,30}$", message = "The ID number format is incorrect")
    @ApiModelProperty(value = "证件号 (格式为印度人证件号格式 示例: 123456789012)")
    private String idCardNumber;

}
