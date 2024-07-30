package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
* 充值取消原因
*
* @author 
*/
    @Data
    @ApiModel(description ="充值取消原因新增编辑请求参数")
    public class CancellationRechargeAddReq implements Serializable {

     @ApiModelProperty("原因")
     private String reason;

            /**
            * 排序
            */
            @ApiModelProperty("排序")
            private Integer sort;





}