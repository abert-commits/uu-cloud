package org.uu.common.pay.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("我的团队Channel筛选框响应实体")
public class MyGroupFilterBoxResponseVO implements Serializable {
    private static final long serialVersionUID = 2598133580563358509L;

    @ApiModelProperty("邀请码ID")
    private Long id;

    @ApiModelProperty("邀请码标题")
    private String title;

    @ApiModelProperty("邀请码")
    private String inviteCode;
}
