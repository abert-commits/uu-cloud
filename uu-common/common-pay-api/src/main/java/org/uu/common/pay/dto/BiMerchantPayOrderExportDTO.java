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
public class BiMerchantPayOrderExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 日期
     */
    @ApiModelProperty("日期")
    private String dateTime;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 下单总笔数
     */
    @ApiModelProperty("下单笔数")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty("成功笔数")
    private Long successOrderNum = 0L;

    /**
     * 总费用
     */
    @ApiModelProperty("费用")
    private BigDecimal totalFee = BigDecimal.ZERO;


}