package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @date 2024/3/15 14:17
 */
@Data
@ApiModel(description = "订单状态统计列表")
public class OrderStatusOverviewListDTO {
    @ApiModelProperty(value = "买入订单")
    private OrderStatusOverviewDTO buyOrderOverview;

    @ApiModelProperty(value = "卖出订单")
    private OrderStatusOverviewDTO sellOrderOverview;

    @ApiModelProperty(value = "合计")
    private OrderStatusOverviewDTO totalOrderOverview;
}
