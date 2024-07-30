package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * app信息维护表
 * </p>
 *
 * @author
 * @since 2024-07-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_info")
public class AppInfo extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * app名称
     */
    private String appName;

    /**
     * app版本号
     */
    private String appVersion;

    /**
     * 是否强制更新0否1是
     */
    private Integer isForcedUpdate;

    /**
     * 更新内容描述
     */
    private String description;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 下载url
     */
    private String downloadUrl;

    /**
     * 设备类型 1-ios 2-android
     */
    private Integer device;

}
