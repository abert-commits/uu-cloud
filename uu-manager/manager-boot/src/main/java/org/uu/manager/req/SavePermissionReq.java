package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Accessors(chain = true)
@ApiModel(description = "保存权限请求参数")
public class SavePermissionReq implements Serializable {

    private Long id;

    @ApiModelProperty(value = "权限名称")
    @NotBlank(message = "name 不为空")
    private String name;

    @ApiModelProperty(value = "菜单id")
    @NotNull(message = "menuId 不为空")
    private Long menuId;

    @ApiModelProperty(value = "method")
    @NotBlank(message = "method 不为空")
    private String method;

    @ApiModelProperty(value = "服务名称")
    @NotBlank(message = "serviceName 不为空")
    private String serviceName;

    @ApiModelProperty(value = "url")
    @NotBlank(message = "url 不为空")
    private String url;

    @ApiModelProperty(value = "按钮标识")
    @NotBlank(message = "按钮标识 不为空")
    private String btnSign;

}
