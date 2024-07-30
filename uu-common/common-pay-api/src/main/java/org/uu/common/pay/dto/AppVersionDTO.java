package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * APP版本管理
 * </p>
 *
 * @author 
 * @since 2024-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_version_manager")
public class AppVersionDTO implements Serializable {

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
     * 强制更新版本
     */
    @ApiModelProperty("强制更新版本")
    private String forceUpdateVersion;

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
     * 设备类型
     */
    @ApiModelProperty("设备类型 1-ios 2-android")
    private Integer device;

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
     * 状态 1-开启 0-关闭
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 创建者
     */
    private String createBy;


}
