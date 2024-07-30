package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Banner信息表
 * </p>
 *
 * @author
 * @since 2024-02-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("banner_info")
public class BannerInfo extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Banner类型
     */
    private String bannerType;

    /**
     * Banner图片URL
     */
    private String bannerImageUrl;

    /**
     * 跳转链接URL
     */
    private String redirectUrl;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态（1为启用，0为禁用）
     */
    private Integer status;

    /**
     * 是否删除 默认值: 0
     */
    private Integer deleted;

    /**
     * 跳转链接（1站内，2站外）
     */
    private Integer linkType;
}
