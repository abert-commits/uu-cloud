package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "代付订单请求参数")
public class PaymentOrderGetInfoReq implements Serializable {
    @ApiModelProperty(value = "主键")
    private Long id;











}
