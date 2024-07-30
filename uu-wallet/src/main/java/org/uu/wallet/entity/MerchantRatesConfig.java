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
 * 商户对应的代收、代付费率设置
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_rates_config")
public class MerchantRatesConfig extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 1:代收 2:代付
     */
    private Integer type;

    /**
     * 代收、代付的具体类型(currency_pay_type的pay_type) 1: 银行卡, 2: USDT, 3: UPI
     */
    private String payType;

    /**
     * currency_pay_type的主键id
     */
    private Long payTypeId;

    /**
     * 代收、代付的具体类型：例如upi、银行
     */
    private String payTypeName;

    /**
     * 费率
     */
    private BigDecimal rates;

    /**
     * 固定手续费
     */
    private BigDecimal fixedFee;
    /**
     * 最小金额
     */
    private BigDecimal moneyMin;
    /**
     * 最大金额
     */
    private BigDecimal moneyMax;

    /**
     * 代付限额 如果大于此值就走手动
     */
    private BigDecimal paymentReminderAmount;

    /**
     * 状态（1为启用，0为禁用）
     */
    private Integer status;

    /**
     * 是否删除 默认值: 0
     */
    private Integer deleted;
}
