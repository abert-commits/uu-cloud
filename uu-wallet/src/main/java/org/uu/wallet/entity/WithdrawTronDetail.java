package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 代付钱包交易记录
 * </p>
 *
 * @author
 * @since 2024-07-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("withdraw_tron_detail")
@Builder
@AllArgsConstructor
public class WithdrawTronDetail extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 交易ID
     */
    private String txid;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 源地址
     */
    private String fromAddress;

    /**
     * 目标地址
     */
    private String toAddress;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 转账状态 0转账中 1成功 2 失败 默认1
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
