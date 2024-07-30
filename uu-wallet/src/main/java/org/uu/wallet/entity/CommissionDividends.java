package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("t_commission_dividends")
public class CommissionDividends implements Serializable {
    private static final long serialVersionUID = 8596139882646728689L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源头会员ID
     */
    private Long fromMember;

    /**
     * 目标会员ID
     */
    private Long toMember;

    /**
     * 记录类型 1-买入返佣 2-卖出返佣 2-分红
     */
    private Integer recordType;

    /**
     * 账变订单号
     */
    private String accountChangeOrderNo;

    /**
     * 订单号(返佣时则存储引起返佣的订单号 分红时不存储)
     */
    private String orderNo;

    /**
     * 奖励金额
     */
    private BigDecimal rewardAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
