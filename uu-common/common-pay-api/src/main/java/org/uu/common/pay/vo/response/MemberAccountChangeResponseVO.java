package org.uu.common.pay.vo.response;

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
@ApiModel("会员账变记录响应实体类")
public class MemberAccountChangeResponseVO implements Serializable {
    private static final long serialVersionUID = -4679616609034523713L;

    @ApiModelProperty("账变类型 1-买入 2-卖出 8-买入奖励 9-卖出奖励 15-买入返佣 18-卖出返佣 16-平台分红 4-人工上分 7-人工下分")
    private String changeMode;

    @ApiModelProperty("账变金额")
    private BigDecimal amountChange;

    @ApiModelProperty("订单号(仅买入卖出账变存在订单号)")
    private String orderNo;

    @ApiModelProperty("账变时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
