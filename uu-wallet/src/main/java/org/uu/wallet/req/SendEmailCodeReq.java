package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author
 */
@Data
@ApiModel(description = "发送短信验证码接口请求参数")
public class SendEmailCodeReq {

    /**
     * 邮箱账号
     */
    @NotNull(message = "Email account cannot be empty")
    @Pattern(regexp = "^(.+)@(.+)$", message = "The email account format is incorrect")
    @ApiModelProperty(value = "邮箱账号 格式为标准邮箱格式 示例: asd@123456.com")
    private String emailAccount;
}