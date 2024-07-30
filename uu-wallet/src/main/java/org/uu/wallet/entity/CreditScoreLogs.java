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
 * 信用分记录表
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("credit_score_logs")
public class CreditScoreLogs extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long memberId;

    /**
     * 事件类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账
     */
    private Integer eventType;

    /**
     * 类型 1-买入 2-卖出
     */
    private Integer tradeType;

    /**
     * 变化前分数
     */
    private BigDecimal beforeScore;

    /**
     * 分数变化
     */
    private BigDecimal changeScore;

    /**
     * 变化后分数
     */
    private BigDecimal afterScore;

    /**
     * 变化类型 1-增加 2-减少
     */
    private Integer changeType;



}
