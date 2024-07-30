package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单事件信息
 *
 * @author lukas
 */
@Data
@ApiModel(description = "订单事件信息")
public class OrderEventReq implements Serializable {
    @ApiModelProperty("事件id")
    private String eventId;

    @ApiModelProperty("参数")
    private String params;
}