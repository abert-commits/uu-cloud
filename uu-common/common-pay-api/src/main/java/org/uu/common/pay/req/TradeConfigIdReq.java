package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易配置表
 *
 *
 */
@Data
@ApiModel(description = "配置信息")
public class TradeConfigIdReq implements Serializable {

     private Long id;


}