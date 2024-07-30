package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "代收订单对象")
public class RechargeEnOrderDTO implements Serializable {


    @ApiModelProperty(value = "id")
    private Long id;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "Create Time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "Merchant Order Number")
    private String merchantOrder;

    @ApiModelProperty(value = "Order Number")
    private String platformOrder;

    @ApiModelProperty(value = "Pay Type")
    private String payType;

    @ApiModelProperty(value = "Order Amount")
    private BigDecimal amount;


    @ApiModelProperty(value = "Cost")
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "Update Time")
    private LocalDateTime updateTime;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "Trade Callback Time")
    private LocalDateTime tradeCallbackTime;

    @ApiModelProperty(value = "Payment Status")
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty(value = "Callback Status")
    private String tradeCallbackStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty(value = "Remark")
    private String remark;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "Merchant Name")
    private String merchantName;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "Merchant Code")
    private String merchantCode;

    /**
     * 完成时间
     */
    @ApiModelProperty(value = "Completion Time")
    private LocalDateTime completionTime;



}