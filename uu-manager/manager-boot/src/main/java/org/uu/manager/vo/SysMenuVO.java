package org.uu.manager.vo;

import lombok.Data;
import org.uu.manager.entity.SysPermission;

import java.io.Serializable;
import java.util.List;


@Data
public class SysMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    private Long id;
    /**
     * 父级菜单id
     */
    private Long parentId;

    /**
    * 菜单名称
    */
    private String name;

    /**
    * 路由路径
    */
    private String path;

    /**
    * 组件路径
    */
    private String component;

    /**
    * 菜单图标
    */
    private String icon;

    /**
    * 排序
    */
    private Integer sort;

    /**
    * 状态：0-禁用 1-开启
    */
    private int visible;

    /**
    * 跳转路径
    */
    private String redirect;

    /**
     * 子节点
     */
    private List<SysMenuVO> children;


    private  List<SysPermission> listPermission;


    public SysMenuVO() {}
}