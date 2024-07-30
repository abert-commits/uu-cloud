package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取当前客服系统接口返回数据")
public class CurrentCustomerServiceSystemVo implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * 客服系统名称
     */
    @ApiModelProperty(value = "客服系统名称")
    private String serviceSystemName;

    /**
     * 客服系统访问链接
     */
    @ApiModelProperty(value = "客服系统访问链接")
    private String serviceSystemUrl;

    /**
     * 客服系统类型
     */
    @ApiModelProperty(value = "客服系统类型, 1: livechat, 2: twak")
    private Integer type;
}