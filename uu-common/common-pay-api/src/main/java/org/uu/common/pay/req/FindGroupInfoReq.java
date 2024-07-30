package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequestHome;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("查询团队信息请求")
@EqualsAndHashCode(callSuper = true)
public class FindGroupInfoReq extends PageRequestHome implements Serializable {

    private static final long serialVersionUID = -3296115188892020231L;

    @ApiModelProperty("当前会员ID")
    @NotNull(message = "Please specify the id of current member")
    private Long currentMemberId;

    @ApiModelProperty("当前会员前几级  默认1")
    @Min(value = 1, message = "The minimum value of countOfBefore is 1")
    private Integer countOfBefore = 1;

    @ApiModelProperty("当前会员后几级  默认1")
    @Min(value = 1, message = "The minimum value of countOfOfter is 1")
    private Integer countOfOfter = 1;
}
