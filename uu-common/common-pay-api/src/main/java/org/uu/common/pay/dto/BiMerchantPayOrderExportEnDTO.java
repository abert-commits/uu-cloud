package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商户代收订单日表导出
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiMerchantPayOrderExportEnDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty("date")
    private String dateTime;

    /**
     * 订单金额
     */
    @ApiModelProperty("money")
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    @ApiModelProperty("orderNum")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty("successOrderNum")
    private Long successOrderNum = 0L;

    /**
     * 总费用
     */
    @ApiModelProperty("totalFee")
    private BigDecimal totalFee = BigDecimal.ZERO;


}