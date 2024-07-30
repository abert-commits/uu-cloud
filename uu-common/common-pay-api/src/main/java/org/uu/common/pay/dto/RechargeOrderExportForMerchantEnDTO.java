package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "代收订单对象")
public class RechargeOrderExportForMerchantEnDTO implements Serializable {


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "createTime")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "platformOrder")
    private String platformOrder;

    /**
     * 商户会员ID
     */
    @ApiModelProperty(value = "externalMemberId")
    private String externalMemberId;


    @ApiModelProperty(value = "merchantOrder")
    private String merchantOrder;

    @ApiModelProperty(value = "amount")
    private String amount;

    @ApiModelProperty(value = "cost")
    private String cost;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "merchantName")
    private String merchantName;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "merchantCode")
    private String merchantCode;


    /**
     * 完成时间
     */
    @ApiModelProperty(value = "completionTime")
    private LocalDateTime completionTime;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "tradeCallbackTime")
    private LocalDateTime tradeCallbackTime;


    @ApiModelProperty(value = "orderStatus")
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty(value = "tradeCallbackStatus")
    private String tradeCallbackStatus;


}