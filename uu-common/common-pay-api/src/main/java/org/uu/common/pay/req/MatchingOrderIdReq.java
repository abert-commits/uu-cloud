package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description ="撮合列表")
public class MatchingOrderIdReq implements Serializable {


    @ApiModelProperty("撮合Id")
    private Long id;







}