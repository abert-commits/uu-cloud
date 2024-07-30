package org.uu.common.pay.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageReturn;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("邀请链接响应实体类")
public class InviteInfoResponseVO implements Serializable {
    private static final long serialVersionUID = 6878326085801017473L;

    @ApiModelProperty("买入奖励比例最小值")
    private Integer nextOneRegisterCount;

    @ApiModelProperty("买入奖励比例最大值")
    private Integer nextTwoRegisterCount;

    @ApiModelProperty("当前用户买入奖励比例")
    private BigDecimal buyRewardRatioOfCurrent;

    @ApiModelProperty("团队成员")
    private PageReturn<InviteInfoDetailResponseVO> inviteLinkList;
}
