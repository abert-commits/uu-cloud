package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

@Data
@ApiModel(description = "代付订单请求参数")
public class PaymentOrderIdReq implements Serializable {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "备注")
    String remark;

    @ApiModelProperty(value = "操作人")
    String opName;










}
