package org.uu.wallet.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取Banner列表返回数据")
public class BannerListVo implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * Banner类型
     */
    @ApiModelProperty(value = "Banner类型")
    private String bannerType;

    /**
     * Banner图片链接
     */
    @ApiModelProperty(value = "Banner图片链接")
    private String bannerImageUrl;

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