package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "Banner信息请求参数")
public class BannerInfoReq {

    /**
     * Banner类型
     */
    @ApiModelProperty(value = "Banner类型")
    @NotBlank(message = "Banner type cannot be blank")
    private String bannerType;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order cannot be negative")
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
    @NotBlank(message = "Banner image name cannot be blank")
    private String bannerImageUrl;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    @NotNull(message = "Status cannot be null")
    @Min(value = 0, message = "Status must be 0 or 1")
    private Integer status;


    /**
     * 跳转链接（1站内，2站外）
     */
    @ApiModelProperty(value = "跳转链接（1站内，2站外）")
    @NotNull(message = "LinkType cannot be null")
    @Min(value = 1, message = "Link type must be 1 (internal) or 2 (external)")
    private Integer linkType;
}
