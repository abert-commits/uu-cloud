package org.uu.manager.vo;


import lombok.Data;
import org.uu.manager.entity.SysMenu;
import org.uu.manager.entity.SysRole;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class SysUserVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户名称
     */
    private String username;

    private String password;

    /**
     * 昵称
     */

    private String nickname;
    /**
     * 手机
     */
    private String mobile;

    /**
     * 性别
     */
    private Integer gender;

    private String avatar;

    private String email;

    private Integer status;

    private List<Long> menuIds;
    /**
     * 用户菜单
     */
    private List<SysMenu> listMenu;
    /**
     * 用户角色
     */
    private List<SysRole> listRole;
    private List<Long> roleIds;
    /**
     * 用户按钮权限
     */
    private List<String> permissions;

    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private Long loginCount;

}
