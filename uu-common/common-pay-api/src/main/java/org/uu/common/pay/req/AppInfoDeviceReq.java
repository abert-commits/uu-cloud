package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "app信息请求参数")
public class AppInfoDeviceReq {

    @ApiModelProperty(value = "设备：2-ios 1-android")
    @NotNull(message = "device cannot be null")
    private Integer device;

}
