package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "新增客服系统信息请求参数")
public class CustomerServiceSystemsReq {

    /**
     * 客服系统类型
     */
    @ApiModelProperty(value = "客服系统类型")
    @NotNull(message = "type cannot be null")
    @Min(value = 0, message = "type cannot be negative")
    private Integer type;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    /**
     * 图标地址
     */
    @ApiModelProperty(value = "图标地址")
//    @NotBlank(message = "iconUrl cannot be blank")
    private String iconUrl;

    /**
     * 客服系统访问链接
     */
    @ApiModelProperty(value = "客服系统访问链接")
    @NotBlank(message = "serviceSystemUrl cannot be blank")
    private String serviceSystemUrl;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
//    @NotNull(message = "active cannot be null")
//    @Min(value = 0, message = "active must be 0 or 1")
    private Integer active;


    /**
     * 客服系统名称
     */
    @ApiModelProperty(value = "客服系统名称")
//    @NotBlank(message = "serviceSystemName cannot be blank")
    private String serviceSystemName;
}
