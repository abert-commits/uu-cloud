package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员手动操作记录
 * </p>
 *
 * @author 
 * @since 2024-02-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MemberLevelWelfareConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级
     */
    @ApiModelProperty("等级")
    private Integer level;

    /**
     * 单次限额
     */
    @ApiModelProperty("单次限额")
    private BigDecimal singleAmountLimit;

    /**
     * 自选买入
     */
    @ApiModelProperty("自选买入")
    private Integer selfSelectionBuy;

    /**
     * 每日任务
     */
    @ApiModelProperty("每日任务")
    private Integer dailyTasks;

    /**
     * 每周任务
     */
    @ApiModelProperty("每周任务")
    private Integer weeklyTasks;

    /**
     * 每月任务
     */
    @ApiModelProperty("每月任务")
    private Integer monthlyTasks;

    /**
     * 修改时间
     */
    @ApiModelProperty("最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}
