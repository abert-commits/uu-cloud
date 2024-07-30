package org.uu.manager.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Data
@Accessors(chain = true)
public class SysPermissionVO implements Serializable {

    private Long id;

    private String name;

    private Long menuId;

    private String urlPerm;

    private String btnSign;

    @ApiModelProperty(value = "method")
    @NotBlank(message = "method 不为空")
    private String method;

    @ApiModelProperty(value = "服务名称")
    @NotBlank(message = "serviceName 不为空")
    private String serviceName;

    @ApiModelProperty(value = "url")
    @NotBlank(message = "url 不为空")
    private String url;

}
