package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "获取Banner信息返回数据")
public class BannerInfoDTO {

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * Banner类型
     */
    @ApiModelProperty(value = "Banner类型")
    private String bannerType;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    private Integer sortOrder;

    /**
     * 跳转链接URL
     */
    @ApiModelProperty(value = "跳转链接URL")
    private String redirectUrl;

    /**
     * Banner图片名称
     */
    @ApiModelProperty(value = "Banner图片名称")
    private String bannerImageUrl;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer status;


    /**
     * 跳转链接（1站内，2站外）
     */
    @ApiModelProperty(value = "跳转链接（1站内，2站外）")
    private Integer linkType;
}
