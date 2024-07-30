package org.uu.common.pay.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("添加邀请链接请求实体类")
public class InviteLinkSaveRequestVO implements Serializable {
    private static final long serialVersionUID = -4827593825143423572L;

    @ApiModelProperty("奖励比例")
    @NotEmpty(message = "The title cannot be empty")
    private String title;
}
