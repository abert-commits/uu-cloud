package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
* 充值取消原因
*
* @author 
*/
    @Data
    @ApiModel(description ="重置取消原因列表")
    public class CancellationRechargePageListReq extends PageRequest {
    @ApiModelProperty("主键")
    private Long id;
            /**
            * 原因
            */
            @ApiModelProperty("原因")
            private String reason;




}