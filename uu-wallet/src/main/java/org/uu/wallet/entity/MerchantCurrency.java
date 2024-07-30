package org.uu.wallet.entity;

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
@TableName("merchant_currency")
public class MerchantCurrency extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 币种符号
     */
    private String currencyCode;

    /**
     * 币种名称
     */
    private String currencyName;
    /**
     * 是否删除 0 未删除 ,1 已删除
     */
    private Integer deleted;
}
