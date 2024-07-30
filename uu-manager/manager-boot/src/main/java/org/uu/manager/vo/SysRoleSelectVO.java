package org.uu.manager.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class SysRoleSelectVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
    * id
    */
    private Long id;

    /**
    * 角色名称
    */
    private String name;
}