package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "获取客服系统信息返回数据")
public class CustomerServiceSystemsDTO {
    
    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 客服系统类型
     */
    @ApiModelProperty(value = "客服系统类型 1: livechat, 2: twak")
    private String type;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    private Integer sortOrder;

    /**
     * 客服系统访问链接
     */
    @ApiModelProperty(value = "客服系统访问链接")
    private String serviceSystemUrl;

    /**
     * 图标地址
     */
    @ApiModelProperty(value = "图标地址")
    private String iconUrl;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer active;

    /**
     * 客服系统名称
     */
    @ApiModelProperty(value = "客服系统名称")
    private String serviceSystemName;
}
