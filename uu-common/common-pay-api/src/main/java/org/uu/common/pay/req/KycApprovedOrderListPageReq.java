package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;

/**
 * @author admin
 * @date 2024/5/3 18:45
 */
@Data
@ApiModel(description = "验证完成的订单列表请求参数")
public class KycApprovedOrderListPageReq extends PageRequest {
    /**
     * 提现开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;


    /**
     * 提现结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;


    /**
     * 买入订单号
     */
    @ApiModelProperty(value = "买入订单号")
    private String buyerOrderId;

    /**
     * 卖出订单号
     */
    @ApiModelProperty(value = "卖出订单号")
    private String sellerOrderId;


    /**
     * 买入会员Id
     */
    @ApiModelProperty(value = "买入会员Id")
    private String buyerMemberId;


    /**
     * 买入会员Id
     */
    @ApiModelProperty(value = "买入会员Id")
    private String sellerMemberId;

    /**
     * 收款人UPI
     */
    @ApiModelProperty(value = "收款人UPI")
    private String recipientUpi;

    /**
     * 付款人UPI
     */
    @ApiModelProperty(value = "付款人UPI")
    private String payerUpi;

    /**
     * UTR
     */
    @ApiModelProperty(value = "utr")
    private String utr;

    /**
     * 交易状态 1-成功
     */
    @ApiModelProperty(value = "交易类型 1-收入 2-支出")
    private String transactionType;


    /**
     * 银行编码
     */
    @ApiModelProperty(value = "bankCode")
    private LocalDateTime bankCode;


    /**
     * kyc订单号
     */
    @ApiModelProperty(value = "orderId")
    private LocalDateTime orderId;




}
