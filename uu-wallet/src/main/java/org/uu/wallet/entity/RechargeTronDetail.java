package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包交易记录
 * </p>
 *
 * @author 
 * @since 2024-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recharge_tron_detail")
public class RechargeTronDetail extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * 交易ID
     */
    private String txid;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 付款地址
     */
    private String fromAddress;

    /**
     * 收款地址
     */
    private String toAddress;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 转账时间, 13位时间戳
     */
    private Long betTime;
}
