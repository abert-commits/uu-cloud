package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "获取活动信息返回数据")
public class ActivityInfoDTO {


    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;


    /**
     * 活动标题
     */
    @ApiModelProperty(value = "活动标题")
    private String activityTitle;


    /**
     * 活动内容
     */
    @ApiModelProperty(value = "活动内容")
    private String activityContent;


    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    private Integer sortOrder;


    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer status;
    
}
