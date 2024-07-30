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
@ApiModel(description = "我的申诉-列表")
public class ViewMyAppealVo implements Serializable {

    /**
     * 申诉状态
     */
    @ApiModelProperty("申诉状态: 1-申诉中 2-申诉成功 3-申诉失败 4-金额错误")
    private Integer appealStatus;

    /**
     * 申诉类型
     */
    @ApiModelProperty("申诉类型: 1-卖出申诉 2-买入申诉")
    private Integer appealType;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal orderAmount;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;
}