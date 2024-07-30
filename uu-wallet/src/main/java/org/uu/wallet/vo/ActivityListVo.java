package org.uu.wallet.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取Activity列表返回数据")
public class ActivityListVo implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * Activity类型
     */
    @ApiModelProperty(value = "Activity类型")
    private String activityType;

    /**
     * Activity图片链接
     */
    @ApiModelProperty(value = "Activity图片链接")
    private String activityImageUrl;

    /**
     * 跳转链接URL
     */
    @ApiModelProperty(value = "跳转链接URL")
    private String redirectUrl;

    /**
     * 跳转链接（1站内，2站外）
     */
    @ApiModelProperty(value = "跳转链接（1站内，2站外）")
    private Integer linkType;
}
