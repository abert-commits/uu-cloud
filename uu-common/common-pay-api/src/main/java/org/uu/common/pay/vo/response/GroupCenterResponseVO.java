package org.uu.common.pay.vo.response;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("团队中心响应实体类")
public class GroupCenterResponseVO implements Serializable {
    private static final long serialVersionUID = 6112076489544220150L;

    @ApiModelProperty("用户ID")
    private Long uid;

    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyRewardRatio;

    @ApiModelProperty("平台分红率")
    private BigDecimal platformDividends;

    @ApiModelProperty("默认邀请码")
    private String defaultInviteCode;

    @ApiModelProperty("累计分红金额")
    private BigDecimal totalDividendsAmount;

    @ApiModelProperty("累计买入金额")
    private BigDecimal totalBuyAmount;

    @ApiModelProperty("累计卖出金额")
    private BigDecimal totalSellAmount;

    @ApiModelProperty("累计买入奖励")
    private BigDecimal totalBuyRewardAmount;

    @ApiModelProperty("累计卖出奖励")
    private BigDecimal totalSellRewardAmount;

    @ApiModelProperty("总奖励")
    private BigDecimal totalRewardAmount;

    @ApiModelProperty("买卖奖励")
    private BigDecimal buyAndSellRewardAmount;

    @ApiModelProperty("今日邀请数量")
    private Integer inviteCountOfToday;

    @ApiModelProperty("团队数量")
    private Integer groupCount;

    @ApiModelProperty("团队奖励")
    private BigDecimal groupAmount;

    @ApiModelProperty("用户层级")
    private Integer level;
}
