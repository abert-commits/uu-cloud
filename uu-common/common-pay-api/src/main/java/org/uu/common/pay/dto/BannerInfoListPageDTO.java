package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "获取Banner列表返回数据")
public class BannerInfoListPageDTO implements Serializable {


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
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重 (小排在前)")
    private Integer sortOrder;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer status;

    /**
     * 最后更新时间
     */
    @ApiModelProperty(value = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    private String updateBy;


    /**
     * 跳转链接（1站内，2站外）
     */
    @ApiModelProperty(value = "跳转链接（1站内，2站外）")
    private Integer linkType;
}