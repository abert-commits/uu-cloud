package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入记录")
public class UsdtBuyOrderVo implements Serializable {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 3: 等待转账, 4: 确认中, 13: 支付过期, 7: 已完成, 10: 购买失败")
    private String status;

    /**
     * USDT数量
     */
    @ApiModelProperty(value = "USDT数量")
    private BigDecimal usdtNum;

    /**
     * ARB数量
     */
    @ApiModelProperty(value = "ARB数量")
    private BigDecimal arbNum;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;
}