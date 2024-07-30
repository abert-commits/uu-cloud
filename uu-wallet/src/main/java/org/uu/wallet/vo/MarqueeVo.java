package org.uu.wallet.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "跑马灯信息")
public class MarqueeVo {

    /**
     * 跑马灯内容
     */
    @ApiModelProperty(value = "跑马灯内容")
    private String content;

    /**
     * 方向: 1向左(默认) 2向右 3向上 4向下
     */
    @ApiModelProperty(value = "方向: 1向左(默认) 2向右 3向上 4向下")
    private Integer direction;

    /**
     * 循环方式 0无限循环 n循环n次
     */
    @ApiModelProperty(value = "循环方式: 0无限循环(默认) n循环n次")
    private Integer loopCount;

    /**
     * 滚动方式:slide（滑动）、scroll（滚动）、alternate（来回切换）
     */
    @ApiModelProperty(value = "滚动方式:slide滑动(默认)、scroll滚动、alternate来回切换")
    private String behavior;
}
