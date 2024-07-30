package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequestHome;

import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "卖出订单列表请求参数")
public class SellOrderListReq extends PageRequestHome {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 1: 等待, 2: 支付中, 3: 完成, 4: 关闭")
    @Pattern(regexp = "^\\d+$", message = "Order status format is incorrect")
    private String orderStatus;

    /**
     * 查询时间 (格式: YYYY-MM-DD)
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Query time format is incorrect")
    @ApiModelProperty(value = "查询时间 (格式: YYYY-MM-DD)")
    private String date;
}
