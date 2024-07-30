package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "IP黑名单请求参数")
public class TradeIpBlackListReq extends PageRequest {

   @ApiModelProperty("行id: 更新时候必传")
   private Long id;

   @ApiModelProperty("ip")
   private String ip;

   /**
    * 备注
    */
   @ApiModelProperty(value = "备注")
   private String remark;


   /**
    * 状态
    */
   @ApiModelProperty(value = "状态 0禁用 1启用")
   private String status;




}