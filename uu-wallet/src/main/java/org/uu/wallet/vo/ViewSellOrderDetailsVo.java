package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@ApiModel(description = "卖出订单详情")
public class ViewSellOrderDetailsVo implements Serializable {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 1:匹配中, 2: 匹配超时, 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 9: 订单失效, 11: 金额错误, 13: 支付超时, 14: 进行中, 15: 已完成")
    private String orderStatus;

    /**
     * 取消原因
     */
    @ApiModelProperty(value = "取消原因")
    private String reason;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 实际到账金额
     */
    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal orderActualAmount;

    /**
     * 奖励
     */
    @ApiModelProperty(value = "奖励")
    private BigDecimal bonus;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间")
    private String paymentTime;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    private String UPI;

    /**
     * 支付凭证
     */
    @ApiModelProperty(value = "支付凭证")
    private String voucher;

    /**
     * 图片
     */
    @ApiModelProperty(value = "图片")
    private String picInfo;

    /**
     * 视频
     */
    @ApiModelProperty(value = "视频")
    private String videoUrl;

}