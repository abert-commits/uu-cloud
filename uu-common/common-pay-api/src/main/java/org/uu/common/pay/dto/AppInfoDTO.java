package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "app信息维护表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppInfoDTO implements Serializable {

    private Long id;

    @ApiModelProperty("app名称")
    private String appName;

    @ApiModelProperty("app版本号")
    private String appVersion;

    @ApiModelProperty("是否强制更新0否1是")
    private Integer isForcedUpdate;

    @ApiModelProperty("更新内容描述")
    private String description;

    @ApiModelProperty("下载url")
    private String downloadUrl;

    @ApiModelProperty("设备类型 1-ios 2-android")
    private Integer device;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}