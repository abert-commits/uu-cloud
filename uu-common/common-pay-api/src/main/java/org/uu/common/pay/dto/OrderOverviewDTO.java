package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @date 2024/3/9 15:37
 */
@Data
@ApiModel(description = "订单概览")
public class OrderOverviewDTO {

    @ApiModelProperty(value = "代收订单")
    private Long merchantCollectionOrderNum;

    @ApiModelProperty(value = "代付订单")
    private Long merchantPaymentOrderNum;

}
