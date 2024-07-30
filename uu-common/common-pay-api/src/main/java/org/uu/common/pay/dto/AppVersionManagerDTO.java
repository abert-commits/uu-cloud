package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Admin
 */
@Data
@ApiModel(description = "会员授权返回")
public class AppVersionManagerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 最新版本号
     */
    @ApiModelProperty("最新版本号")
    private String latestVersion;

    /**
     * 最小支持版本号
     */
    @ApiModelProperty("最小支持版本号")
    private String minVersion;

    /**
     * app下载url
     */
    @ApiModelProperty("app下载url")
    private String downloadUrl;

    /**
     * 更新文案
     */
    @ApiModelProperty("更新文案")
    private String updateDescription;

    /**
     * 是否有更新
     */
    @ApiModelProperty("是否有更新")
    private Boolean isUpdate;

    /**
     * 是否强制更新
     */
    @ApiModelProperty("是否强制更新")
    private Boolean isForceUpdate;

    /**
     * app 文件大小
     */
    @ApiModelProperty("app 文件大小")
    private String fileSize;

    /**
     * app md5值
     */
    @ApiModelProperty("app md5值")
    private String md5;

    /**
     * 设备类型
     */
    @ApiModelProperty("设备类型 1-ios 2-android")
    private Integer device;



}
