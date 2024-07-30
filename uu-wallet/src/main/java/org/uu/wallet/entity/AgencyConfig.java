package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 代理配置表
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_agency_config")
public class AgencyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 代理名称
     */
    private String agencyName;

    /**
     * 佣金比例(%)
     */
    private BigDecimal commissionRatio;

    /**
     * 平台分红(%)
     */
    private BigDecimal platformDividend;

    /**
     * 删除状态 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
