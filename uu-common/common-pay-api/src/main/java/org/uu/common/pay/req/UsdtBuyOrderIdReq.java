package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入返回")
public class UsdtBuyOrderIdReq implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("备注")
    private String remark;



}