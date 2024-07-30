package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "usdt配置返回信息")
public class UsdtConfigDTO implements Serializable {
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 网络
     */
    @ApiModelProperty("网络")
    private String networkProtocol;

    /**
     * usdt地址
     */
    @ApiModelProperty("usdt地址")
    private String usdtAddr;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}