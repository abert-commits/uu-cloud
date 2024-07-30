package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 
*
* @author 
*/
    @Data
    @ApiModel(description = "usdt查询请求参数")
    public class UsdtConfigQueryReq implements Serializable {
    @ApiModelProperty("主键")
    private long  id;


            /**
            * 状态
            */

            @ApiModelProperty("状态,0禁止 1开启")
            private String status;




}