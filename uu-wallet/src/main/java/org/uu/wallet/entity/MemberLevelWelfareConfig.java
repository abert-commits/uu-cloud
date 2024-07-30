package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会员等级福利配置
 * </p>
 *
 * @author 
 * @since 2024-04-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_level_welfare_config")
public class MemberLevelWelfareConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 单次限额
     */
    private BigDecimal singleAmountLimit;

    /**
     * 自选买入
     */
    private Integer selfSelectionBuy;

    /**
     * 每日任务
     */
    private Integer dailyTasks;

    /**
     * 每周任务
     */
    private Integer weeklyTasks;

    /**
     * 每月任务
     */
    private Integer monthlyTasks;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;


}
