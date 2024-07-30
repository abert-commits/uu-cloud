package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 * 订单统计信息
 */
@Data
public class OrderInfoVo implements Serializable {

    /**
     * 实际总金额
     */
    private BigDecimal actualAmount;

    /**
     * 总订单数量
     */
    private Long totalNum;

    /**
     * 总费用
     */
    private BigDecimal totalCost;
}