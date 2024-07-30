package org.uu.wallet.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequestHome;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "查看交易类型请求参数")
public class ViewTransactionHistoryReq extends PageRequestHome implements Serializable {

    private static final long serialVersionUID = -6869562825605022963L;

    /**
     * 交易类型
     */
    @ApiModelProperty(value = "交易类型, 取值说明: 1:买入 2:卖出")
    @NotNull(message = "Please specify the transaction type")
    private Integer transactionType;

    /**
     * 买入交易子类型
     */
    @ApiModelProperty(value = "买入交易子类型, 取值说明: 11:INR 12:USDT充值")
    private Integer subTransactionType;

    /**
     * 交易状态
     */
    @ApiModelProperty(value = "交易状态, 取值说明: 3:待支付 7:已完成 8:已取消 -1/null: 全部   默认-1")
    private Integer transactionStatus = -1;

    /**
     * 查询时间 (格式: YYYY-MM-DD)
     */
    @ApiModelProperty(value = "查询时间 (格式: YYYY-MM-DD)")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
