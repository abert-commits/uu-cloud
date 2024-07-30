package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入返回")
public class UsdtBuyOrderGetInfoReq implements Serializable {

   @ApiModelProperty("主键")
   private long id;




}