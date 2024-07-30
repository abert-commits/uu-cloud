package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 信用分配置表
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Data
@ApiModel(description = "会员黑名单返回")
public class CreditScoreConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件id
     */
    @ApiModelProperty("事件id")
    private Integer eventId;

    /**
     * 触发事件
     */
    @ApiModelProperty("触发事件 1-支付超时 2-已完成 3-提交申诉 4-被申诉 5-确认超时")
    private Integer triggerEvent;

    /**
     * 类型 1-买入 2-卖出
     */
    @ApiModelProperty("类型 1-买入 2-卖出")
    private Integer tradeType;

    /**
     * 事件类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账
     */
    @ApiModelProperty("事件类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账")
    private Integer eventType;

    /**
     * 分数变化
     */
    @ApiModelProperty("分数变化")
    private String score;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("更新事件")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;


}
