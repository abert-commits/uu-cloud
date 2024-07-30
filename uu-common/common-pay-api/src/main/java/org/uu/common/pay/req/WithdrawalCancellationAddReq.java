package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    @ApiModel(description = "取款取消原因添加和编辑请求参数")
    public class WithdrawalCancellationAddReq implements Serializable {
        @ApiModelProperty("主键")
        private long id;



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