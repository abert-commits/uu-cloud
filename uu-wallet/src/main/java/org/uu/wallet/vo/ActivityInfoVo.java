package org.uu.wallet.vo;

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
@ApiModel(description = "前台-获取活动信息返回数据")
public class ActivityInfoVo implements Serializable {

    @ApiModelProperty("活动id")
    private Long id;

    /**
     * 活动标题
     */
    @ApiModelProperty("活动标题")
    private String activityTitle;

    /**
     * 活动内容
     */
    @ApiModelProperty("活动内容")
    private String activityContent;

    /**
     * 活动封面图片地址
     */
    @ApiModelProperty("活动封面图片地址")
    private String coverImageUrl;

    /**
     * 活动时间
     */
    @ApiModelProperty("活动时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
