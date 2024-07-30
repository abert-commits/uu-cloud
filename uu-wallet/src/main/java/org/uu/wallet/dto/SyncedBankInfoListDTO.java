package org.uu.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncedBankInfoListDTO implements Serializable {

    /**
     * 银行编码
     */
    @ApiModelProperty(value = "银行编码")
    @NotBlank(message = "bankCode cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCode format is incorrect")
    private String bankCode;


    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    @NotBlank(message = "bankName cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankName format is incorrect")
    private String bankName;


    /**
     * 持卡人姓名
     */
    @ApiModelProperty(value = "持卡人姓名")
    @NotBlank(message = "bankCardOwner cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCardOwner format is incorrect")
    private String bankCardOwner;


    /**
     * 银行卡号
     */
    @ApiModelProperty(value = "银行卡号")
    @NotBlank(message = "bankCardNumber cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCardNumber format is incorrect")
    private String bankCardNumber;


    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    @NotBlank(message = "mobileNumber cannot be empty")
    @Pattern(regexp = "^\\d{8,13}$", message = "mobileNumber format is incorrect")
    private String mobileNumber;


    /**
     * 邮箱
     */
//    @NotNull(message = "email cannot be empty")
    @Pattern(regexp = "^(.+)@(.+)$", message = "The email account format is incorrect")
    @ApiModelProperty(value = "邮箱账号 (格式为标准邮箱格式 示例: asd@123456.com)")
    @NotBlank(message = "email cannot be empty")
    private String email;

    /**
     * ifscCode
     */
//    @NotNull(message = "email cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "ifscCode format is incorrect")
    @ApiModelProperty(value = "ifscCode")
    @NotBlank(message = "ifscCode cannot be empty")
    private String ifscCode;
}
