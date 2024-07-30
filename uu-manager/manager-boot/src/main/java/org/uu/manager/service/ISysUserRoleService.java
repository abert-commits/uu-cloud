package org.uu.manager.service;



import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.entity.SysUserRole;

import java.util.List;


public interface ISysUserRoleService extends IService<SysUserRole> {

    /**
     * 获取用户绑定的角色IDs
     * @param userId
     * @return
     */
    List<Long> selectRoleIds(Long userId);

    /**
     * 根据用户ID删除角色绑定关系
     * @param userId
     */
    void deleteByUserId(Long userId);
}
