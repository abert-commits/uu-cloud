package org.uu.manager.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.entity.*;
import org.uu.manager.mapper.SysMenuMapper;
import org.uu.manager.req.CommonReq;
import org.uu.manager.req.SaveMenuReq;
import org.uu.manager.service.*;
import org.uu.manager.vo.SysMenuSelectVO;
import org.uu.manager.vo.SysMenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    private final ISysRoleMenuService roleMenuService;
    private final ISysUserRoleService userRoleService;
    private final ISysPermissionService permissionService;
    private final ISysRolePermissionService rolePermissionService;

    @Override
    public void deletes(List<Long> ids) {
        while (CollectionUtil.isNotEmpty(ids)) {
            this.baseMapper.deleteBatchIds(ids);
            List<SysMenu> sysMenus = lambdaQuery().in(SysMenu::getParentId, ids).select(SysMenu::getId).list();
            if (CollectionUtil.isNotEmpty(sysMenus)) {
                ids = sysMenus.stream().map(SysMenu::getId).collect(Collectors.toList());
            } else {
                ids = null;
            }
        }
    }

    @Override
    public SysMenu updateMenu(SaveMenuReq req) {
        AssertUtil.notEmpty(req.getId(), ResultCode.PARAM_VALID_FAIL);
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(req, sysMenu);
        updateById(sysMenu);
        return sysMenu;
    }

    @Override
    public List<SysMenuVO> listTree() {
        List<SysPermission> listSysPerm = permissionService.lambdaQuery().list();
        Map<String,List<SysPermission>> map  =  listSysPerm.stream().collect(Collectors.groupingBy(SysPermission::getMenuIdStr));

        List<SysMenu> menus = lambdaQuery()
                .eq(SysMenu::getVisible, GlobalConstants.STATUS_ON)
                .orderByAsc(SysMenu::getSort).list();
        if (CollectionUtil.isNotEmpty(menus)) {
            menus.stream().forEach(
                    e->{
                        e.setListPermission(map.get(e.getId().toString())==null?new ArrayList<SysPermission>():map.get(e.getId().toString()));
                    }
            );
            return flushMenuVOs(menus, GlobalConstants.ROOT_MENU_ID);
        }
        return Collections.emptyList();
    }

    @Override
    public SysMenu createMenu(SaveMenuReq req) {
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(req, sysMenu);
        save(sysMenu);
        return sysMenu;
    }

    @Override
    public List<SysMenuVO> loadMenus() {
        List<SysMenu> menus = lambdaQuery().orderByAsc(SysMenu::getSort).list();
        if (CollectionUtil.isNotEmpty(menus)) {
            List<SysMenuVO> menuVOS = flushMenuVOs(menus, GlobalConstants.ROOT_MENU_ID);
            return menuVOS;
        }
        return Collections.emptyList();
    }

    /**
     * 组装菜单
     *
     * @param menus
     * @param parentId
     * @return
     */
    private List<SysMenuVO> flushMenuVOs(List<SysMenu> menus, Long parentId) {
        List<SysMenuVO> childMenus = new ArrayList<>();
        for (SysMenu sysMenu : menus) {
            if (parentId.equals(sysMenu.getParentId())) {
                SysMenuVO sysMenuVO = new SysMenuVO();
                BeanUtils.copyProperties(sysMenu, sysMenuVO);
                childMenus.add(sysMenuVO);
            }
        }
        if (CollectionUtil.isNotEmpty(childMenus)) {
            for (SysMenuVO sysMenuVO : childMenus) {
                List<SysMenuVO> childChildMenus = flushMenuVOs(menus, sysMenuVO.getId());
                sysMenuVO.setChildren(childChildMenus);
            }
            return childMenus;
        }
        return Collections.emptyList();
    }

    /**
     * 组装菜单select
     *
     * @param menus
     * @param parentId
     * @return
     */
    private List<SysMenuSelectVO> flushMenuSelectVOs(List<SysMenu> menus, Long parentId) {
        List<SysMenuSelectVO> childMenus = new ArrayList<>();
        for (SysMenu sysMenu : menus) {
            if (parentId.equals(sysMenu.getParentId())) {
                SysMenuSelectVO sysMenuVO = new SysMenuSelectVO();
                sysMenuVO.setId(sysMenu.getId());
                sysMenuVO.setLabel(sysMenu.getName());
                childMenus.add(sysMenuVO);
            }
        }
        if (CollectionUtil.isNotEmpty(childMenus)) {
            for (SysMenuSelectVO sysMenuVO : childMenus) {
                List<SysMenuSelectVO> childChildMenus = flushMenuSelectVOs(menus, sysMenuVO.getId());
                sysMenuVO.setChildren(childChildMenus);
            }
            return childMenus;
        }
        return Collections.emptyList();
    }

    @Override
    public List<SysMenuSelectVO> select(int status) {
        List<SysMenu> menus = lambdaQuery()
                .eq(status != -1, SysMenu::getVisible, status)
                .orderByAsc(SysMenu::getSort).list();
        if (CollectionUtil.isNotEmpty(menus)) {
            List<SysMenuSelectVO> menuVOS = flushMenuSelectVOs(menus, GlobalConstants.ROOT_MENU_ID);
            return menuVOS;
        }
        return Collections.emptyList();
    }

    @Override
    public SysMenuVO getDetail(Long id) {
        SysMenu sysMenu = getById(id);
        AssertUtil.notEmpty(sysMenu, ResultCode.DATA_NOT_FOUND);
        SysMenuVO sysMenuVO = new SysMenuVO();
        BeanUtils.copyProperties(sysMenu, sysMenuVO);
        return sysMenuVO;
    }

    @Override
    public List<SysMenu> listRoleMenu(Long roleId) {
        List<SysRoleMenu> list = roleMenuService.lambdaQuery().eq(SysRoleMenu::getRoleId, roleId).list();
        List<SysRolePermission> listRolePermission = rolePermissionService.lambdaQuery().in(SysRolePermission::getRoleId, roleId).list();
        List<Long> idList = list.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        List<Long> permIds = listRolePermission.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
        if(permIds.size()==0) return new ArrayList<SysMenu>();
        List<SysPermission> listPermission = permissionService.lambdaQuery().in(SysPermission::getId,permIds).list();
        Map<String,List<SysPermission>> map  =  listPermission.stream().collect(Collectors.groupingBy(SysPermission::getMenuIdStr));
        List<SysMenu> listMenu = this.lambdaQuery().in(SysMenu::getId, idList).list();
        listMenu.stream().forEach(
                e->{
                    e.setListPermission(map.get(e.getId().toString())==null?new ArrayList<SysPermission>():map.get(e.getId().toString()));
                }
        );
        return listMenu;


    }

    @Override
    public List<SysMenu> updateRoleMenu(Long roleId, CommonReq req) {
        // 删除之前绑定的信息
        roleMenuService.getBaseMapper().delete(roleMenuService.lambdaQuery().eq(SysRoleMenu::getRoleId, roleId).getWrapper());
        rolePermissionService.getBaseMapper().delete(rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId).getWrapper());
        // 添加新的绑定信息
        if (CollectionUtil.isNotEmpty(req.getIds())) {
            List<SysRoleMenu> roleMenus = req.getIds().stream().map(menuId -> new SysRoleMenu(menuId, roleId)).collect(Collectors.toList());
            roleMenuService.saveBatch(roleMenus);
        }
        if (CollectionUtil.isNotEmpty(req.getListPermission())) {
            List<SysRolePermission> rolePermissions = req.getListPermission().stream().map(sysPermission -> new SysRolePermission(sysPermission.getId(), roleId)).collect(Collectors.toList());
            rolePermissionService.saveBatch(rolePermissions);
        }

        List<SysRoleMenu> list = roleMenuService.lambdaQuery().in(SysRoleMenu::getRoleId, roleId).list();
        List<SysRolePermission> listRolePermission = rolePermissionService.lambdaQuery().in(SysRolePermission::getRoleId, roleId).list();
        List<Long> idList = list.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        List<Long> permIds = listRolePermission.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<SysPermission> listPermission = permissionService.lambdaQuery().in(SysPermission::getId,permIds).list();
        Map<String,List<SysPermission>> map  =  listPermission.stream().collect(Collectors.groupingBy(SysPermission::getMenuIdStr));
        List<SysMenu> listMenu = this.lambdaQuery().in(SysMenu::getId, idList).list();
        listMenu.stream().forEach(
                e->{
                    e.setListPermission(map.get(e.getId().toString())==null?new ArrayList<SysPermission>():map.get(e.getId().toString()));
                }
        );
        return listMenu;





    }

    @Override
    public SysMenu updateMenuStatus(Long id, int status) {
        lambdaUpdate().eq(SysMenu::getId, id).set(SysMenu::getVisible, status).update();
        SysMenu sysMenu = lambdaQuery().eq(SysMenu::getId, id).one();
        return sysMenu;
    }

    @Override
    public List<SysMenu> currentUser() {
        List<SysMenu> listMenu = new ArrayList<SysMenu>();
        List<SysRolePermission> listPerm = new ArrayList<SysRolePermission>();
        List<SysMenu> menus = lambdaQuery().orderByAsc(SysMenu::getSort).list();
        if (CollectionUtil.isNotEmpty(menus)) {
            // 过滤当前用户角色绑定菜单
            Long currentUserId = UserContext.getCurrentUserId();
            List<Long> roleIds = userRoleService.selectRoleIds(currentUserId);

            if (CollectionUtil.isNotEmpty(roleIds)) {
                List<SysRoleMenu> list = roleMenuService.lambdaQuery().in(SysRoleMenu::getRoleId, roleIds).list();
                List<SysRolePermission> listRolePermission = rolePermissionService.lambdaQuery().in(SysRolePermission::getRoleId, roleIds).list();

                List<Long> idList = list.stream()
                        .map(SysRoleMenu::getMenuId)
                        .collect(Collectors.toList());

                List<Long> permIds = listRolePermission.stream()
                        .map(SysRolePermission::getPermissionId)
                        .collect(Collectors.toList());
                List<SysPermission> listPermission = permissionService.lambdaQuery().in(SysPermission::getId,permIds).list();
                Map<String,List<SysPermission>> map  =  listPermission.stream().collect(Collectors.groupingBy(SysPermission::getMenuIdStr));
                listMenu = this.lambdaQuery().in(SysMenu::getId, idList).list();
                listMenu.stream().forEach(
                        e->{
                            e.setListPermission(map.get(e.getId().toString())==null?new ArrayList<SysPermission>():map.get(e.getId().toString()));
                        }
                );
                if (CollectionUtil.isNotEmpty(listMenu)) {
                    return listMenu;
                }
                return listMenu;
            }

        }
        return listMenu;
    }
}
