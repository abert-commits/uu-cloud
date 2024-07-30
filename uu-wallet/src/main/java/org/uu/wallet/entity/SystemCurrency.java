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
 * 货币配置表
 * </p>
 *
 * @author 
 * @since 2024-07-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("system_currency")
public class SystemCurrency extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 币种符号
     */
    private String currencyCode;

    /**
     * 币种ID(火币)
     */
    private String coinId;

    /**
     * 币种名称
     */
    private String currencyName;

    /**
     * 币种ID2(火币)
     */
    private String currencyId;

    /**
     * 代收支付方式
     */
    private String paymentType;

    /**
     * 代付类型
     */
    private String withdrawType;

    /**
     * USDT最低汇率
     */
    private BigDecimal usdtMin;

    /**
     * USDT最高汇率
     */
    private BigDecimal usdtMax;

    /**
     * USDT自动汇率
     */
    private BigDecimal usdtAuto;

    /**
     * USDT矫正汇率
     */
    private BigDecimal usdtCorrect;

    /**
     * 使用汇率标记 1:自动 2:矫正汇率
     */
    private Integer rateFlag;

    /**
     * 时区
     */
    private String timeZone;

    /**
     * 是否可用 0 可用 ,1 不可用 
     */
    private Integer enableFlag;

    /**
     * 是否删除 0 未删除 ,1 已删除 
     */
    private Integer deleted;
}
