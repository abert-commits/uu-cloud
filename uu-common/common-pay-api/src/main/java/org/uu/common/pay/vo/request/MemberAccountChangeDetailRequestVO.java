package org.uu.common.pay.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("账变详情请求实体类")
public class MemberAccountChangeDetailRequestVO implements Serializable {
    private static final long serialVersionUID = 8466319123699196384L;

    @ApiModelProperty(value = "账变类型 1-买入 2-卖出 8-买入奖励 9-卖出奖励 15-买入返佣 18-卖出返佣 16-平台分红 4-人工上分 7-人工下分")
    @NotNull(message = "Please specify the change type")
    private Integer changeType;

    @ApiModelProperty("订单号")
    @NotEmpty(message = "Please specify the orderNo")
    private String orderNo;
}
