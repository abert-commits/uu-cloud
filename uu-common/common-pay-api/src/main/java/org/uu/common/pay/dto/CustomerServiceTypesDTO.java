package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "获取客服系统类型列表返回数据")
public class CustomerServiceTypesDTO {

    /**
     * 客服系统类型id
     */
    @ApiModelProperty("客服系统类型id, 1: livechat, 2: twak")
    private String typeCode;

    /**
     * 客服系统类型名称
     */
    @ApiModelProperty("客服系统类型名称")
    private String typeName;

}
