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
@ApiModel("团队中心-我的团队响应实体类")
public class MyGroupResponseVO implements Serializable {
    private static final long serialVersionUID = -3712460974137784303L;


    @ApiModelProperty("团队总卖出")
    private BigDecimal totalGroupAmount;

    @ApiModelProperty("邀请码")
    private String inviteCode;

    @ApiModelProperty("团队成员")
    private PageReturn<GroupDetailResponseVO> groupList;
}
