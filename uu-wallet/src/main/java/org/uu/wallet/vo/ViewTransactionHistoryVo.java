package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "交易记录-列表")
public class ViewTransactionHistoryVo implements Serializable {

    private static final long serialVersionUID = 5496310529467665767L;
    /**
     * 交易状态
     */
    @ApiModelProperty("交易状态, 取值说明: 3:待支付 7:已完成 8:已取消")
    private String transactionStatus;

    /**
     * 时间
     */
    @ApiModelProperty("时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * INR或者USDT金额
     */
    @ApiModelProperty("INR/USDT金额")
    private BigDecimal inrOrUsdtAmount;


    @ApiModelProperty("ARB金额")
    private BigDecimal arbAmount;

    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String platformOrder;

    /**
     * 交易类型
     */
    @ApiModelProperty("交易类型 1:买入 2:卖出")
    private Integer transactionType;

    /**
     * 买入交易子类型: 11:INR 12:USDT充值
     */
    @ApiModelProperty("买入交易子类型, 取值说明: 11:INR 12:USDT充值")
    private Integer subTransactionType;
}