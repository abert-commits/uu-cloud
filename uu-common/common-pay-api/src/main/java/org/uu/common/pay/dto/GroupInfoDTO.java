package org.uu.common.pay.dto;

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
@ApiModel("团队信息DTO实体类")
public class GroupInfoDTO implements Serializable {
    private static final long serialVersionUID = 2111945925984504319L;

    @ApiModelProperty("团队关系 1-上级 2-下级")
    private Integer groupRelation;

    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("手机号")
    private String mobileNumber;

    @ApiModelProperty("买入返佣金额")
    private BigDecimal buyCommissionAmount;

    @ApiModelProperty("卖出返佣金额")
    private BigDecimal sellCommissionAmount;

    @ApiModelProperty("买入金额")
    private BigDecimal buyAmount;

    @ApiModelProperty("卖出金额")
    private BigDecimal sellAmount;

    @ApiModelProperty("注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
