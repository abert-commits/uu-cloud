package org.uu.manager.service;



import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.entity.SysMenu;
import org.uu.manager.req.CommonReq;
import org.uu.manager.req.SaveMenuReq;
import org.uu.manager.vo.SysMenuSelectVO;
import org.uu.manager.vo.SysMenuVO;

import java.util.List;


public interface ISysMenuService extends IService<SysMenu> {

    List<SysMenuVO> loadMenus();

    /**
     * 创建菜单
     * @param req
     */
    SysMenu createMenu(SaveMenuReq req);

    /**
     * 菜单树
     * @return
     */
    List<SysMenuVO> listTree();

    /**
     * 更新菜单
     * @param req
     */
    SysMenu updateMenu(SaveMenuReq req);

    /**
     * 批量删除
     * @param ids
     */
    void deletes(List<Long> ids);

    /**
     * 获取菜单select
     * @return
     */
    List<SysMenuSelectVO> select(int status);

    /**
     * 获取菜单详情
     * @param id
     * @return
     */
    SysMenuVO getDetail(Long id);

    /**
     * 查询角色绑定的菜单
     * @param roleId
     * @return
     */
    List<SysMenu> listRoleMenu(Long roleId);

    /**
     * 更新角色绑定的菜单
     * @param roleId
     * @param req
     */
    public List<SysMenu> updateRoleMenu(Long roleId, CommonReq req);

    SysMenu updateMenuStatus(Long id, int status);

    List<SysMenu> currentUser();
}
