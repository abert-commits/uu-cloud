package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "UPI收款信息")
public class UpiCollectionInfoVo implements Serializable {

    @ApiModelProperty(value = "收款信息ID")
    private Long id;

    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    @ApiModelProperty(value = "UPI_Name")
    private String upiName;

    @ApiModelProperty(value = "手机号码")
    private String mobileNumber;

    @ApiModelProperty(value = "是否默认收款信息（0：否，1：是）")
    private Integer defaultStatus;

    @ApiModelProperty(value = "付款类型 取值说明, 1: 印度银行卡, 3: 印度UPI")
    private Integer type;
}