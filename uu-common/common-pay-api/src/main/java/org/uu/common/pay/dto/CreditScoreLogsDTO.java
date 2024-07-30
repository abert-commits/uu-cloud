package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 信用分记录表
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Data
@ApiModel(description = "会员黑名单返回")
public class CreditScoreLogsDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户ID
     */
    @ApiModelProperty("用户id")
    private Long memberId;

    /**
     * 事件类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账
     */
    @ApiModelProperty("事件类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账")
    private Integer eventType;

    /**
     * 类型 1-买入 2-卖出
     */
    @ApiModelProperty("类型 1-买入 2-卖出")
    private Integer tradeType;

    /**
     * 变化前分数
     */
    @ApiModelProperty("变化前分数")
    private BigDecimal beforeScore;

    /**
     * 分数变化
     */
    @ApiModelProperty("分数变化")
    private BigDecimal changeScore;

    /**
     * 变化后分数
     */
    @ApiModelProperty("变化后分数")
    private BigDecimal afterScore;

    @ApiModelProperty(value = "变化类型 1-增加 2-减少")
    private Integer changeType;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
