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
@ApiModel(description = "充值列表返回")
public class CollectionEnOrderDTO implements Serializable {


    @ApiModelProperty("id")
    private Long id;


    @ApiModelProperty("Member ID")
    private String memberId;

    @ApiModelProperty("Member Account")
    private String memberAccount;

    @ApiModelProperty("Create Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

//    /**
//     * 支付方式 默认值: UPI
//     */
//    private String payType;

    /**
     * 商户订单号
     */
    @ApiModelProperty("Merchant Order Number")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty("Platform Order Number")
    private String platformOrder;

    /**
     * 订单金额
     */
    @ApiModelProperty("Order Amount")
    private BigDecimal amount;



    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("Order Status")
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty("Trade Callback Status")
    private String tradeCallbackStatus;

    /**
     * 商户号
     */
    @ApiModelProperty("Merchant Code")
    private String merchantCode;




    /**
     * 交易回调时间
     */
    @ApiModelProperty("Trade Callback Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;





    /**
     * UTR
     */
    @ApiModelProperty("utr")
    private String utr;

    /**
     * 奖励
     */
    @ApiModelProperty("Reward")
    private String bonus;

    /**
     * 实际金额
     */
    @ApiModelProperty("Actual Amount")
    private BigDecimal actualAmount;




    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private BigDecimal completeDuration;


    /**
     * 完成时间
     */
    @ApiModelProperty("Completion Duration")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 商户名称
     */
    @ApiModelProperty("Merchant Name")
    private String merchantName;

    /**
     * 凭证
     */
    @ApiModelProperty("Voucher")
    private String voucher;



}