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
@ApiModel(description = "校验手机号是否被使用接口请求参数")
public class CheckPhoneNumberAvailabilityReq {

    /**
     * 手机号码
     */
    @NotNull(message = "Phone number can not be blank")
    @Pattern(regexp = "^\\d{8,13}$", message = "Mobile phone number format is incorrect")
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    private String mobileNumber;
}