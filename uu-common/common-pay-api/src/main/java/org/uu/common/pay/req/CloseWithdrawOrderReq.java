package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "关闭代付订单请求")
@Validated
public class CloseWithdrawOrderReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商户订单号(三方订单号)")
    private String merchantOrder;

    @ApiModelProperty(value = "代付平台订单号)")
    private String platformOrderNo;
}