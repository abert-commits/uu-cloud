package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "获取公告链接返回数据")
public class AnnouncementLinkDTO {

    /**
     * 公告内容
     */
    @ApiModelProperty(value = "公告链接")
    private String announcementLink;


}
