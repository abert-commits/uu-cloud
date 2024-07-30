package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequestHome;

import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "买入订单列表请求参数")
public class BuyOrderListReq extends PageRequestHome {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 10: 买入失败, 11: 金额错误, 13: 支付超时")
    @Pattern(regexp = "^\\d+$", message = "Invalid order status format")
    private String orderStatus;

    /**
     * 查询时间 (格式: YYYY-MM-DD)
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid query time format")
    @ApiModelProperty(value = "查询时间 (格式: YYYY-MM-DD)")
    private String date;

}
