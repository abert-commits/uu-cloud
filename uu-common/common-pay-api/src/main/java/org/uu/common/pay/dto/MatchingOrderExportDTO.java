package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description = "匹配订单返回导出")
public class MatchingOrderExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("撮合ID")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("撮合时间")
    private LocalDateTime createTime;

    /**
     * 充值会员ID
     */
    @ApiModelProperty("买入会员ID")
    private String collectionMemberId;

    /**
     * 充值平台订单号
     */
    @ApiModelProperty("买入订单号")
    private String collectionPlatformOrder;

    /**
     * 提现会员ID
     */
    @ApiModelProperty("卖出会员ID")
    private String paymentMemberId;


    /**
     * 提现平台订单号
     */
    @ApiModelProperty("卖出订单号")
    private String paymentPlatformOrder;

    /**
     * 订单提交金额
     */
    @ApiModelProperty("订单金额")
    private String orderSubmitAmount;

    /**
     * 订单实际金额
     */
    @ApiModelProperty("实际金额")
    private String orderActualAmount;


    @ApiModelProperty("UTR")
    private String utr;



    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 支付时间
     */
    @ApiModelProperty("支付时间")
    private LocalDateTime paymentTime;



    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;






}