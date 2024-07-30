package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    @ApiModel(description = "提现取消原因")
    public class WithdrawalCancellationIdReq implements Serializable {
    @ApiModelProperty("主键")
    private Long id;









}