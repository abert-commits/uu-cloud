package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 分红配置表
 * </p>
 *
 * @author Parker
 * @since 2024-07-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("t_dividend_config")
public class DividendConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分红配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分红临界点
     */
    private Long criticalPoint;

    /**
     * 奖励比例
     */
    private BigDecimal rewardRatio;

    /**
     * 分红等级
     */
    private Integer dividendsLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
