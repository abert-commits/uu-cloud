package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("卖出中的订单VO实体类")
public class OnSellingOrderVO implements Serializable {
    private static final long serialVersionUID = -7766262672015318551L;

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("订单状态 3-待支付 7-已完成 8-已取消")
    private String orderStatus;

    @ApiModelProperty("订单实际金额")
    private BigDecimal actualAmount;

    @ApiModelProperty("ARB金额")
    private BigDecimal arbAmount;

    @ApiModelProperty("订单创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
