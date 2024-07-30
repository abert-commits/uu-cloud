package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "在正常收款的收款信息")
public class NormalCollectionInfoVo implements Serializable {

    /**
     * 收款信息ID
     */
    @ApiModelProperty(value = "收款信息ID")
    private Long id;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name")
    private String upiName;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String mobileNumber;

    /**
     * 是否默认收款信息
     */
    @ApiModelProperty(value = "是否默认收款信息（0：否，1：是）")
    private Integer defaultStatus;
}