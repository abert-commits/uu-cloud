package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "公告信息请求参数")
public class AnnouncementInfoReq {

    /**
     * 公告标题
     */
    @ApiModelProperty(value = "公告标题")
    @NotBlank(message = "announcementTitle cannot be blank")
    private String announcementTitle;

    /**
     * 公告内容
     */
    @ApiModelProperty(value = "公告内容")
    @NotBlank(message = "announcementContent cannot be blank")
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
    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    /**
     * 状态（1为启用，0为禁用）
     */
    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    @NotNull(message = "Status cannot be null")
    @Min(value = 0, message = "Status must be 0 or 1")
    private Integer status;
}
