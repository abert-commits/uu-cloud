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
    @ApiModel(description = "USDT配置列表")
    public class UsdtConfigReq implements Serializable {
    @ApiModelProperty("主键")
    private long  id;

            /**
            * 网络歇息
            */

            @ApiModelProperty("网络信息")
            private String networkProtocol;

            /**
            * usdt地址
            */
            @ApiModelProperty("usdt地址")
            private String usdtAddr;




            /**
            * 状态
            */
            @ApiModelProperty("状态,0禁止 1开启")
           private String status;




            /**
            * 备注
            */
            @ApiModelProperty("备注")
    private String remark;


}