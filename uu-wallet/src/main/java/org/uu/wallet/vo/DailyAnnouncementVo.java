package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "获取每日公告内容返回数据")
public class DailyAnnouncementVo implements Serializable {


    /**
     * 是否查看过每日公告状态
     */
    @ApiModelProperty(value = "是否查看过每日公告状态 0:未看过  1:已看过")
    private Integer dailyAnnouncementStatus = 0;


    /**
     * 每日公告内容
     */
    @ApiModelProperty(value = "每日公告内容")
    private String dailyAnnouncementContent;
}