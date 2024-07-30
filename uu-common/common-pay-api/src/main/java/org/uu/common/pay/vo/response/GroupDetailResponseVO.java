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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("团队中心-团队详情实体类")
public class GroupDetailResponseVO implements Serializable {
    private static final long serialVersionUID = 3837856672935025801L;

    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("会员名称")
    private String memberName;

    @ApiModelProperty("来源渠道")
    private String fromChannel;

    @ApiModelProperty("返佣比例")
    private BigDecimal commissionRatio;

    @ApiModelProperty("贡献金额")
    private BigDecimal contributionAmount;

    @ApiModelProperty("是否在线 0-在线 1-不在线")
    private Integer online;

    @ApiModelProperty("下级数量")
    private Integer countOfChild;

    @ApiModelProperty("上级UID")
    private Long parentUID;

    @ApiModelProperty("注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerTime;
}
