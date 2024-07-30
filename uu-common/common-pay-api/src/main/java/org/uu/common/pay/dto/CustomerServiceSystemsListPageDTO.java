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
@ApiModel(description = "获取客服系统列表返回数据")
public class CustomerServiceSystemsListPageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * 客服系统名称
     */
    @ApiModelProperty(value = "客服系统名称")
    private String serviceSystemName;

    /**
     * 客服系统类型
     */
    @ApiModelProperty(value = "客服系统类型 1: livechat, 2: twak")
    private String type;

    /**
     * 图标地址
     */
    @ApiModelProperty(value = "图标地址")
    private String iconUrl;

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
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer active;

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

}