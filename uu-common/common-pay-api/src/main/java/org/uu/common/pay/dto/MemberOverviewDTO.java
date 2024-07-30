package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @date 2024/3/9 15:37
 */
@Data
@ApiModel(description = "用户在线概览")
public class MemberOverviewDTO {

    @ApiModelProperty(value = "在线人数")
    private Long onlineMemberCount;

    @ApiModelProperty(value = "委托卖单人数")
    private Long delegationMemberCount;


    @ApiModelProperty(value = "委托卖单资金池")
    private Long delegationAmount;

}
