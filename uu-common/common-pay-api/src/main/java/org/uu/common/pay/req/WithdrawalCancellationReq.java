package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    @ApiModel(description = "提现取消原因")
    public class WithdrawalCancellationReq extends PageRequest {



    @ApiModelProperty("原因")
    private String reason;






}