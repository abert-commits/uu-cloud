package org.uu.wallet.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class CreditScoreLogsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ApiModelProperty("用户id")
    private Long memberId;

    /**
     * 事件名称
     */
    @ApiModelProperty("事件名称 1-买入-支付超时 2-买入-自动完成 3-买入-申诉成功 4-买入-申诉失败 5-买入-申诉失败 6-买入-申诉失败 7-卖出-超过48小时未处理 8-卖出-手动确认到账 9-卖出-申诉成功 10-卖出-申诉失败 11-卖出-申诉失败 12-卖出-申诉失败")
    private Integer eventName;


    /**
     * 分数变化
     */
    @ApiModelProperty("分数变化")
    private String changeScore;


    @ApiModelProperty(value = "变化类型 1-增加 2-减少")
    private Integer changeType;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
