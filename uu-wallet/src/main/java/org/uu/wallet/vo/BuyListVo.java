package org.uu.wallet.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "买入列表返回数据")
public class BuyListVo implements Serializable {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;
}