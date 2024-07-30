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
@ApiModel(description ="买入订单支付接口请求参数")
public class CollectionOrderGetInfoReq implements Serializable {


    @ApiModelProperty("主键")
    private Long id;





}