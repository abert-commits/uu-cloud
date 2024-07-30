package org.uu.manager.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.manager.entity.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@ApiModel(description = "保存角色请求参数")
public class SaveSysRoleReq extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */

    private Long id;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    @NotBlank(message = "name 不能为空")
    private String name;

    /**
     * 角色编码
     */
    @ApiModelProperty(value = "角色编码")
    @NotBlank(message = "code 不能为空")
    private String code;

    /**
     * 显示顺序
     */
    @ApiModelProperty(value = "显示顺序")
    @NotNull(message = "sort 不能为空")
    private Integer sort;

    /**
     * 角色状态：0-正常；1-停用
     */
    @ApiModelProperty(value = "角色状态")
    private int status;

    /**
     * 逻辑删除标识：0-未删除；1-已删除
     */
    @ApiModelProperty(value = "逻辑删除标识")
    private int deleted;

    public SaveSysRoleReq() {
    }
}