package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "添加收款信息请求参数")
public class FrontendCollectionInfoReq {

    //upi收款信息---------
    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID 格式为: 本地用户名@银行handle")
//    @NotBlank(message = "upiId cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+$", message = "upiId format is incorrect")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name 格式为: 纯用户名")
//    @NotBlank(message = "upiName cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "upiName format is incorrect")
    private String upiName;


    //银行收款信息---------
    /**
     * 银行编码
     */
    @ApiModelProperty(value = "银行编码")
//    @NotBlank(message = "upiName cannot be empty")
//    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCode format is incorrect")
    private String bankCode;


    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
//    @NotBlank(message = "upiName cannot be empty")
//    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankName format is incorrect")
    private String bankName;


    /**
     * 持卡人姓名
     */
    @ApiModelProperty(value = "持卡人姓名")
//    @NotBlank(message = "upiName cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCardOwner format is incorrect")
    private String bankCardOwner;


    /**
     * 银行卡号
     */
    @ApiModelProperty(value = "银行卡号")
//    @NotBlank(message = "upiName cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "bankCardNumber format is incorrect")
    private String bankCardNumber;


    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
//    @NotBlank(message = "upiName cannot be empty")
    @Pattern(regexp = "^\\d{8,13}$", message = "mobileNumber format is incorrect")
    private String mobileNumber;


    /**
     * 邮箱
     */
//    @NotNull(message = "email cannot be empty")
    @Pattern(regexp = "^(.+)@(.+)$", message = "The email account format is incorrect")
    @ApiModelProperty(value = "邮箱账号 (格式为标准邮箱格式 示例: asd@123456.com)")
    private String email;

    /**
     * ifscCode
     */
//    @NotNull(message = "email cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[\\s._][a-zA-Z0-9]+)*$", message = "ifscCode format is incorrect")
    @ApiModelProperty(value = "ifscCode")
    private String ifscCode;

    /**
     * 付款类型
     */
    @NotNull(message = "type cannot be empty")
    @Min(value = 0, message = "type cannot be negative")
    @ApiModelProperty(value = "付款类型 取值说明, 1: 印度银行卡, 3: 印度UPI")
    private Integer type;

    /**
     * 手机号码
     */
//    @NotNull(message = "手机号码不能为空")
//    @Pattern(regexp = "^\\d{8,13}$", message = "手机号码格式不正确")
//    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
//    private String mobileNumber;


    /**
     * 验证码
     */
    @NotNull(message = "verification code must be filled")
    @ApiModelProperty(value = "验证码 (格式为6位随机数 示例: 123456)")
    @Pattern(regexp = "\\d{6}", message = "Verification code error")
    private String verificationCode;


//    /**
//     * 最小收款限额
//     */
//    @DecimalMin(value = "0.00", message = "最小收款限额格式不正确")
//    @ApiModelProperty(value = "最小收款限额")
//    private BigDecimal minimumAmount;
//
//    /**
//     * 最大收款限额
//     */
//    @DecimalMin(value = "0.00", message = "最大收款限额格式不正确")
//    @ApiModelProperty(value = "最大收款限额")
//    private BigDecimal maximumAmount;
//
//    /**
//     * 每日收款限额
//     */
//    @ApiModelProperty(value = "每日收款限额")
//    @DecimalMin(value = "0.00", message = "每日收款限额格式不正确")
//    private BigDecimal dailyLimitAmount;
//
//    /**
//     * 每日收款次数
//     */
//    @ApiModelProperty(value = "每日收款笔数")
//    @Min(value = 0, message = "每日收款次数格式不正确")
//    private Integer dailyLimitCount;
}
