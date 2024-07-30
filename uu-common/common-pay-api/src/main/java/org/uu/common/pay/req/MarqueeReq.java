package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "首页跑马灯请求参数")
public class MarqueeReq {

    /**
     * 跑马灯内容
     */
    @ApiModelProperty(value = "跑马灯内容")
    @NotBlank(message = "marquee cannot be blank")
    private String content;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    @Min(value = 0, message = "Status must be 0 or 1")
    private Integer status;
}
