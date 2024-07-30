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
    @ApiModel(description ="重置取消原因取消参数")
    public class CancellationRechargeIdReq implements Serializable {
            @ApiModelProperty("主键")
            private Long id;




}