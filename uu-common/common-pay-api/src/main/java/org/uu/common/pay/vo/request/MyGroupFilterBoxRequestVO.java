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
@ApiModel("我的团队Channel筛选框请求实体")
public class MyGroupFilterBoxRequestVO implements Serializable {
    private static final long serialVersionUID = -2697586483827204791L;

    @ApiModelProperty("页面类型 1-下级(默认) 2-下下级")
    private Integer pageType = 1;
}
