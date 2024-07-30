package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    @ApiModel(description = "取款取消原因添加和编辑请求参数")
    public class WithdrawalCancellationCreateReq implements Serializable {



            /**
            * 原因
            */
            @ApiModelProperty("原因")
    private String reason;

            /**
            * 排序
            */
            @ApiModelProperty("排序")
    private Integer sort;






}