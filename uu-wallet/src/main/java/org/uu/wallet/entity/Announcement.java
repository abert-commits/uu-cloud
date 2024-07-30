package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动配置
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Announcement extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    private String announcementTitle;

    /**
     * 活动链接
     */
    private String announcementContent;

    /**
     * 活动列表海报
     */
    private String activityPoster;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态（1为启用，0为禁用）
     */
    private Integer status;

    /**
     * 删除表示: 0未删除，1已删除
     */
    private Integer deleted;
}
