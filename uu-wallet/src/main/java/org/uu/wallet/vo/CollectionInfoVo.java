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
@ApiModel(description = "收款信息")
public class CollectionInfoVo implements Serializable {

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


//    /**
//     * 每日收款限额
//     */
//    @ApiModelProperty(value = "每日收款限额")
//    private BigDecimal dailyLimitAmount;
//
//    /**
//     * 每日收款笔数
//     */
//    @ApiModelProperty(value = "每日收款笔数")
//    private Integer dailyLimitCount;

//    /**
//     * 最小金额
//     */
//    @ApiModelProperty(value = "最小金额")
//    private BigDecimal minimumAmount;
//
//    /**
//     * 最大金额
//     */
//    @ApiModelProperty(value = "最大金额")
//    private BigDecimal maximumAmount;
//
//    /**
//     * 已收款金额
//     */
//    @ApiModelProperty(value = "已收款金额")
//    private BigDecimal collectedAmount;
//
//    /**
//     * 已收款次数
//     */
//    @ApiModelProperty(value = "已收款笔数")
//    private Integer collectedCount;

//    /**
//     * 收款状态
//     */
//    @ApiModelProperty("收款状态，取值说明： 1:正常, 0: 停用")
//    private String collectedStatus;
    
//    /**
//     * 今日成功收款笔数
//     */
//    @ApiModelProperty("今日成功收款笔数")
//    private Integer todaySuccessCollectedCount;
//
//    /**
//     * 今日成功收款金额
//     */
//    @ApiModelProperty("今日成功收款金额")
//    private BigDecimal todaySuccessCollectedAmount;
}