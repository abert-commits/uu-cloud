package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客服系统配置表
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("customer_service_systems")
public class CustomerServiceSystems extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客服系统名称
     */
    private String serviceSystemName;

    /**
     * 客服系统访问链接
     */
    private String serviceSystemUrl;

    /**
     * 是否启用, 0: 关闭, 1: 开启
     */
    private Integer active;

    /**
     * 排序权重, 小排在前
     */
    private Integer sortOrder;

    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 客服系统类型
     */
    private Integer type;

    /**
     * 是否删除 默认值: 0
     */
    private Integer deleted;
}
