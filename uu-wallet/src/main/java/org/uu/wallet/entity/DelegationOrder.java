package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 委托订单表
 * </p>
 *
 * @author 
 * @since 2024-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("delegation_order")
public class DelegationOrder extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * 委托金额
     */
    private BigDecimal amount;

    /**
     * 支付类型, 1:银行卡, 3:UPI, 5:银行卡和UPI
     */
    private String paymentType;

    /**
     * 委托时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime delegationTime;

    /**
     * 订单状态: 1-匹配中, 2-已完成, 3-已取消
     */
    private Integer status;

    /**
     * 剩余金额
     */
    private BigDecimal remainingAmount;

}
