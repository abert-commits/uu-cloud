package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "银行卡收款信息")
public class BankCardCollectionInfoVo implements Serializable {

    @ApiModelProperty(value = "收款信息ID")
    private Long id;

    @ApiModelProperty(value = "银行编码")
    private String bankCode;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "持卡人姓名")
    private String bankCardOwner;

    @ApiModelProperty(value = "银行卡号")
    private String bankCardNumber;

    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    private String mobileNumber;

    @ApiModelProperty(value = "邮箱账号 (格式为标准邮箱格式 示例: asd@123456.com)")
    private String email;

    @ApiModelProperty(value = "ifscCode")
    private String ifscCode;

    @ApiModelProperty(value = "付款类型 取值说明, 1: 印度银行卡, 3: 印度UPI")
    private Integer type;

    @ApiModelProperty(value = "是否默认收款信息（0：否，1：是）")
    private Integer defaultStatus;
}