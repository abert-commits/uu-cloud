package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 
*
* @author 
*/
    @Data
    @ApiModel(description = "usdt配置请求参数")
    public class UsdtConfigPageReq extends PageRequest {


            /**
            * 网络歇息
            */
    private String networkProtocol;

            /**
            * usdt地址
            */
    private String usdtAddr;


            /**
            * 状态
            */
            @ApiModelProperty("状态,0禁止 1开启")
    private String status;




            /**
            * 备注
            */
    private String remark;


}