package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 波场钱包地址表
 * </p>
 *
 * @author 
 * @since 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tron_wallet")
public class TronWallet extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * USDT地址
     */
    private String address;

    /**
     * USDT地址_hex
     */
    private String hexAddress;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * TRX余额
     */
    private BigDecimal trxBalance;

    /**
     * USDT余额
     */
    private BigDecimal usdtBalance;

    /**
     * 账户类型 1,中转账户 2.资金账户 3.出款账户
     */
    private Integer walletType;

    /**
     * 是否删除 0 未删除 ,1 已删除 
     */
    private Integer deleted;
}
