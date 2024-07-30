package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "添加买入金额列表订单到redis")
public class OrderDetail {

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 最小金额
     */
    private BigDecimal minimumAmount;

    /**
     * 最大金额
     */
    private BigDecimal maximumAmount;

    /**
     * 支付方式
     */
    private String  payType;

    /**
     * 订单号
     */
    private String platformOrder;

    /**
     * 头像
     */
    private Integer avatar;
}
