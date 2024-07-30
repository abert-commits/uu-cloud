package org.uu.common.pay.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequestHome;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("会员账变记录请求实体类")
public class MemberAccountChangeRequestVO extends PageRequestHome implements Serializable {
    private static final long serialVersionUID = 618386985182220276L;

    @ApiModelProperty(value = "账变类型 1-买入 2-卖出 8-买入奖励 9-卖出奖励 15-买入返佣 18-卖出返佣 16-平台分红 4-人工上分 7-人工下分 3-USDT充值 -1/null-全部 默认-1")
    private Integer changeType = -1;

    @ApiModelProperty(value = "账变日期 为空则查询全部 否则响应该日期对应的账变记录")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate changeDate;
}
