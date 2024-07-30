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
@ApiModel(description = "申诉详情")
public class AppealOrderVo implements Serializable {

    /**
     * 订单时间
     */
    @ApiModelProperty(value = "订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 申诉时间
     */
    @ApiModelProperty(value = "申诉时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;

    /**
     * utr
     */
    @ApiModelProperty(value = "utr")
    private String utr;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String orderNo;

    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * UPI
     */
    @ApiModelProperty(value = "UPI")
    private String upi;

    /**
     * 申诉原因
     */
    @ApiModelProperty(value = "申诉原因")
    private String reason;

    /**
     * 图片信息
     */
    @ApiModelProperty(value = "图片信息")
    private String picInfo;

    /**
     * 视频url
     */
    @ApiModelProperty(value = "视频url")
    private String videoUrl;

}