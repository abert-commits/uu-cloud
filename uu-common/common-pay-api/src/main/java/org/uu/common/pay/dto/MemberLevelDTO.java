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
public class MemberLevelDTO implements Serializable {

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
     * 信誉分
     */
    @ApiModelProperty("信誉分")
    private BigDecimal creditScore;

    /**
     * 卖出次数
     */
    @ApiModelProperty("卖出次数")
    private Integer sellNum;

    /**
     * 买入次数
     */
    @ApiModelProperty("买入次数")
    private Integer buyNum;

    /**
     * 买入成功率
     */
    @ApiModelProperty("买入成功率")
    private BigDecimal buySuccessRate;


}
