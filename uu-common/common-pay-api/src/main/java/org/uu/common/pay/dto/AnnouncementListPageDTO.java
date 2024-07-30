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
@ApiModel(description = "获取活动配置列表返回数据")
public class AnnouncementListPageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;


    /**
     * 活动标题
     */
    @ApiModelProperty(value = "活动标题")
    private String announcementTitle;


    /**
     * 活动链接
     */
    @ApiModelProperty(value = "活动链接")
    private String announcementContent;


    /**
     * 活动列表海报
     */
    @ApiModelProperty(value = "活动列表海报")
    private String activityPoster;


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
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;


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