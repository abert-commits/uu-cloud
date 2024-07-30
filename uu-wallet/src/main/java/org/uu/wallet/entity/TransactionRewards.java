package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 交易奖励表
 * </p>
 *
 * @author 
 * @since 2024-03-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("transaction_rewards")
public class TransactionRewards extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 奖励金额
     */
    private BigDecimal rewardAmount;

    /**
     * 奖励类型, 1-买入奖励, 2-卖出奖励
     */
    private Integer type;
}
