package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "代收订单列表返回数据")
public class CollectionOrderListVo implements Serializable {

    /**
     * 订单时间
     */
    @ApiModelProperty(value = "订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 商户订单号
     */
    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty(value = "平台订单号")
    private String platformOrder;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 支付方式 (类型)
     */
    @ApiModelProperty(value = "类型")
    private String payType;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "实际金额")
    private BigDecimal collectedAmount;

    /**
     * 订单费率
     */
    @ApiModelProperty(value = "订单费率")
    private BigDecimal orderRate;

    /**
     * 费用
     */
    @ApiModelProperty(value = "费用")
    private BigDecimal cost;

    /**
     * 回调时间
     */
    @ApiModelProperty(value = "回调时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callbackTime;

    /**
     * 支付状态
     */
    @ApiModelProperty(value = "支付状态")
    private String orderStatus;

    /**
     * 回调状态
     */
    @ApiModelProperty(value = "回调状态")
    private String callbackStatus;
}
