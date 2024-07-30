package org.uu.manager.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.manager.entity.SysRole;
import org.uu.manager.req.RoleListPageReq;
import org.uu.manager.req.SaveSysRoleReq;
import org.uu.manager.vo.SysRoleSelectVO;
import org.uu.manager.vo.SysRoleVO;

import java.util.List;


public interface ISysRoleService extends IService<SysRole> {

    /**
     * 角色列表select
     *
     * @return
     */
    List<SysRoleSelectVO> roleSelect();

    /**
     * 列表分页
     * @param req
     * @return
     */
    PageReturn<SysRoleVO> listPage(RoleListPageReq req);

    /**
     * 创建角色
     * @param req
     */
    SysRole createRole(SaveSysRoleReq req);

    /**
     * 更新角色
     * @param req
     */
    SysRole updateRole(SaveSysRoleReq req);

    /**
     * 删除角色
     */
    void deletes(List<Long> ids);


    /**
     * 更新角色状态
     * @param id
     * @param status
     */
    SysRole updateStatus(Long id, int status);
}
