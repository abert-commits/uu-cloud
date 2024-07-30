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
@ApiModel(description = "卖出订单列表返回")
public class PaymentOrderEnListPageDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;


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
     * 匹配订单号
     */
    @ApiModelProperty("Matching order number")
    private String matchOrder;


    /**
     * 会员ID
     */
    @ApiModelProperty("Member ID")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("Member Account")
    private String memberAccount;


    /**
     * 订单金额
     */
    @ApiModelProperty("Order Amount")
    private BigDecimal amount;

    /**
     * 实际金额
     */
    @ApiModelProperty("Actual Amount")
    private BigDecimal actualAmount;

    /**
     * 订单状态 默认状态: 待匹配
     */
    @ApiModelProperty("Order Status")
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty("Trade Callback Status")
    private String tradeCallbackStatus;


    /**
     * 商户名称
     */
    @ApiModelProperty("Merchant Name")
    private String merchantName;


    /**
     * 奖励
     */
    @ApiModelProperty("Reward")
    private BigDecimal bonus;

    /**
     * 匹配时长
     */
    @ApiModelProperty("Matching Duration")
    private String matchDuration;

    /**
     * 完成时长
     */
    @ApiModelProperty("Completion Duration")
    private String completeDuration;


    /**
     * 凭证
     */
    @ApiModelProperty("Voucher")
    private String voucher;

    /**
     * 备注
     */
    @ApiModelProperty("Remark")
    private String remark;

    @ApiModelProperty("Create Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    @ApiModelProperty("Completion Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * UTR
     */
    @ApiModelProperty("UTR")
    private String utr;

    /**
     * current_pay_type
     */
    @ApiModelProperty("当前支付类型")
    private String currentPayType;

}