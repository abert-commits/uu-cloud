package org.uu.common.pay.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequestHome;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("我的团队请求实体类")
@EqualsAndHashCode(callSuper = true)
public class MyGroupRequestVO extends PageRequestHome implements Serializable {
    private static final long serialVersionUID = -2063351931058784137L;

    @ApiModelProperty("页面类型 1-下级(默认) 2-下下级")
    private Integer pageType = 1;

    @ApiModelProperty("来源ID -1/null表示全部(默认)")
    private Long channelId = -1L;

    @ApiModelProperty("根据注册时间排序 1-降序(默认) 2-升序")
    private Integer orderByRegisterTime = 1;
}
