package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.manager.entity.SysPermission;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel(description = "角色保存菜单和按钮参数")
public class CommonReq {
    @ApiModelProperty(value = "ids")
    @Size(min = 1, message = "ids 不能为空")
    private List<Long> ids;
    private List<SysPermission> listPermission;
}
