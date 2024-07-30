package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotBlank;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "banner实体类")
public class BannerPageReq extends PageRequest {
    /**
     * Banner类型 01:轮播图，02:类型图
     */
    @ApiModelProperty(value = "Banner类型")
    @NotBlank(message = "bannerType cannot be blank")
    private String bannerType;

}
