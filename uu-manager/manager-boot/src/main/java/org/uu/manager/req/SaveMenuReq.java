package org.uu.manager.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.manager.entity.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@ApiModel(description = "保存菜单请求参数")
public class SaveMenuReq extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 菜单名称
     */
    @ApiModelProperty(value = "菜单名称")
    @NotBlank(message = "name 不能为空")
    private String name;

    /**
     * 父菜单id
     */
    @ApiModelProperty(value = "父菜单id")
    @NotNull(message = "parentId 不能为空")
    private Long parentId;

    /**
     * 路由路径
     */
    @ApiModelProperty(value = "路由路径")
    @NotBlank(message = "path 不能为空")
    private String path;

    /**
     * 组件路径
     */
    @ApiModelProperty(value = "组件路径")
    @NotBlank(message = "component 不能为空")
    private String component;

    /**
     * 菜单图标
     */
    @ApiModelProperty(value = "菜单图标")
    private String icon;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    @NotNull(message = "sort 不能为空")
    private Integer sort;

    /**
     * 状态：0-禁用 1-开启
     */
    @ApiModelProperty(value = "状态")
    private int visible;

    /**
     * 跳转路径
     */
    @ApiModelProperty(value = "跳转路径")
    private String redirect;


    public SaveMenuReq() {
    }
}