package org.uu.common.pay.vo.request;

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
@ApiModel("分页请求实体")
public class PageRequestVO implements Serializable {
    private static final long serialVersionUID = 8723200504920270436L;

    @ApiModelProperty("当前页面(默认1)")
    private Integer pageNum = 1;

    @ApiModelProperty("当前页显示记录数(默认10)")
    private Integer pageSize = 10;
}
