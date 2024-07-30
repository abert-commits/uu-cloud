package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@ApiModel("白名单参数")
public class SysWhiteReq extends PageRequest {

    /**
     * ip地址
     */
    @ApiModelProperty(value = "IP地址")
    private String ip;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * chuang'j
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态 0禁用 1启用")
    private String status;

    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改人时间")
    private Date updateTime;

    /**
     * 后台类别
     */
    @ApiModelProperty(value = "后台类别")
    private String clientType;

}
