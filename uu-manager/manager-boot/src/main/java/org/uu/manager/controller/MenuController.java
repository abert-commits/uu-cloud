package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.entity.SysMenu;
import org.uu.manager.entity.SysPermission;
import org.uu.manager.req.CommonReq;
import org.uu.manager.req.SaveMenuReq;
import org.uu.manager.service.ISysMenuService;
import org.uu.manager.service.ISysPermissionService;
import org.uu.manager.service.ISysRolePermissionService;
import org.uu.manager.vo.SysMenuSelectVO;
import org.uu.manager.vo.SysMenuVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/menu")
@Slf4j
@RequiredArgsConstructor
@Api(description = "菜单控制器")
public class MenuController {
    private final ISysMenuService sysMenuService;
    private final ISysPermissionService permissionService;




    /**
     * 菜单详情
     */
    @PostMapping("/{id}")
    @ApiOperation(value = "获取菜单详情")
    public RestResult<SysMenuVO> getDetail(@PathVariable Long id) {
        try {
            SysMenuVO sysMenuVO = sysMenuService.getDetail(id);
            return RestResult.ok(sysMenuVO);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("获取菜单详情失败");
        }

    }

    /**
     * 获取菜单数据列表
     *
     * @return
     */
    @PostMapping("/list")
    @ApiOperation(value = "获取菜单数据列表")
    public RestResult<List<SysMenuVO>> list() {
        try {
            List<SysMenuVO> sysMenuVOS = sysMenuService.loadMenus();
            return RestResult.ok(sysMenuVOS);
        }catch (Exception e){
            e.printStackTrace();
            return RestResult.failed("获取菜单数据列表失败");
        }
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @ApiOperation(value = "创建菜单")
    @SysLog(title = "菜单控制器",content = "创建菜单")
    public RestResult<Object> createMenu(@Validated @RequestBody SaveMenuReq req) {
        try {
            SysMenu sysMenu = sysMenuService.createMenu(req);
            return RestResult.ok(sysMenu);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("创建菜单失败");
        }
    }

    /**
     * 菜单树
     */
    @PostMapping("/listTree")
    @ApiOperation(value = "菜单树")
    public RestResult<List<SysMenuVO>> listTree() {
        List<SysMenuVO> menuVOS = sysMenuService.listTree();


        return RestResult.ok(menuVOS);
    }

    /**
     * 更新菜单
     */
    @PutMapping
    @ApiOperation(value = "更新菜单")
    @SysLog(title = "菜单控制器",content = "更新菜单")
    public RestResult updateMenu(@Validated @RequestBody SaveMenuReq req) {
        SysMenu sysMenu  = sysMenuService.updateMenu(req);
        permissionService.refreshPermRolesRules();
        return RestResult.ok(sysMenu);
    }

    /**
     * 更新菜单
     */
    @PatchMapping("/{id}/{status}")
    @ApiOperation(value = "更新菜单状态")
    @SysLog(title = "菜单控制器",content = "更新菜单状态")
    public RestResult updateMenuStatus(@PathVariable Long id, @PathVariable int status) {
        SysMenu sysMenu  = sysMenuService.updateMenuStatus(id, status);
        return RestResult.ok(sysMenu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{ids}")
    @ApiOperation(value = "删除菜单")
    @SysLog(title = "菜单控制器",content = "删除菜单")
    public RestResult deletes(@PathVariable List<Long> ids) {
        try {
            sysMenuService.deletes(ids);
            return RestResult.ok();
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("删除菜单失败");
        }
    }

    /**
     * 菜单select
     */

    @PostMapping("/select/{status}")
    @ApiOperation(value = "菜单select")
    public RestResult<List<SysMenuSelectVO>> select(@PathVariable int status) {
        try {
            List<SysMenuSelectVO> selectVOS = sysMenuService.select(status);
            return RestResult.ok(selectVOS);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("菜单selects失败");
        }
    }

    /**
     * 查询角色绑定的菜单
     */
    @PostMapping("/role/{roleId}")
    @ApiOperation(value = "查询角色绑定的菜单")
    public RestResult<List<SysMenu>> listRoleMenu(@PathVariable Long roleId) {
        try {
            List<SysMenu> listMenu = sysMenuService.listRoleMenu(roleId);

            return RestResult.ok(listMenu);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("查询角色绑定的菜单失败");
        }
    }


    /**
     * 更新角色绑定的菜单
     */
    @PutMapping("/role/{roleId}")
    @ApiOperation(value = "更新角色绑定的菜单")
    @SysLog(title = "菜单控制器",content = "更新角色绑定的菜单")
    public RestResult updateRoleMenu(@PathVariable Long roleId, @RequestBody CommonReq req) {
        try {
           List<SysMenu> listmenu = sysMenuService.updateRoleMenu(roleId, req);
            permissionService.refreshPermRolesRules();
            return RestResult.ok(listmenu);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("更新角色绑定的菜单失败");
        }
    }

    /**
     * 获取当前用户绑定的菜单
     */

    @PostMapping("/currentUser")
    @ApiOperation(value = "获取当前用户绑定的菜单")
    public RestResult<List<SysMenu>> currentUser() {
        try {
            List<SysMenu> menus = sysMenuService.currentUser();
            return RestResult.ok(menus);
        }catch(Exception e){
            e.printStackTrace();
            return RestResult.failed("获取当前用户绑定的菜单是失败");
        }

    }
}
