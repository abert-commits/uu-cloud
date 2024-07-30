package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "代收订单详情返回数据")
public class CollectionOrderInfoVo {

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String username;

    /**
     * 支付通道
     */
    @ApiModelProperty(value = "支付通道")
    private String channel;

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
     * 订单费用
     */
    @ApiModelProperty(value = "订单费用")
    private BigDecimal cost;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * 回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "完成时间")
    private LocalDateTime callbackTime;

    /**
     * 订单状态
     */
    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

    /**
     * 类型
     */
    @ApiModelProperty(value = "类型")
    private String payType;

}
