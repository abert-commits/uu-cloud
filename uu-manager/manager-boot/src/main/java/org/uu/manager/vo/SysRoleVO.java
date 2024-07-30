package org.uu.manager.vo;


import lombok.Data;

import java.io.Serializable;


@Data
public class SysRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    private Long id;

    /**
    * 角色名称
    */
    private String name;

    /**
    * 角色编码
    */
    private String code;

    /**
    * 显示顺序
    */
    private Integer sort;

    /**
    * 角色状态：0-正常；1-停用
    */
    private int status;

    public SysRoleVO() {}
}