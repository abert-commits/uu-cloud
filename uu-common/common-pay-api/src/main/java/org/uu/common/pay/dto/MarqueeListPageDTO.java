package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取marquee列表返回数据")
public class MarqueeListPageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 公告标题
     */
    @ApiModelProperty(value = "跑马灯内容")
    private String content;

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