package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 币种对应的代收代付类型
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("currency_pay_type")
public class CurrencyPayType extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 币种
     */
    private String currency;

    /**
     * 币种表的主键id
     */
    private Long currencyId;

    /**
     * 1:代收 2:代付
     */
    private Integer type;

    /**
     * 代收、代付的类型的code
     */
    private String payType;

    /**
     * 代收、代付的具体类型：例如upi、银行
     */
    private String payTypeName;

    /**
     * 删除表示: 0未删除，1已删除
     */
    private Integer deleted;
}
