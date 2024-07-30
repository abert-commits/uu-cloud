package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "app信息请求参数")
public class AppInfoReq {

    private Long id;

    @ApiModelProperty(value = "app名称")
    private String appName;

    @ApiModelProperty(value = "app版本号")
    private String appVersion;

    @ApiModelProperty(value = "是否强制更新0否1是")
    private Integer isForcedUpdate;

    @ApiModelProperty(value = "更新内容描述")
    private String description;


    @ApiModelProperty(value = "下载url")
    private String downloadUrl;

    @ApiModelProperty(value = "设备")
    private Integer device;

}
