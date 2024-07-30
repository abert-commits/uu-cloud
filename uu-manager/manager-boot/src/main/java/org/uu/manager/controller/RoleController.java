package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.entity.SysRole;
import org.uu.manager.req.RoleListPageReq;
import org.uu.manager.req.SaveSysRoleReq;
import org.uu.manager.service.ISysPermissionService;
import org.uu.manager.service.ISysRoleService;
import org.uu.manager.vo.SysRoleSelectVO;
import org.uu.manager.vo.SysRoleVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/role")
@Slf4j
@RequiredArgsConstructor
@Api(description = "角色控制器")
public class RoleController {
    private final ISysRoleService roleService;
    private final ISysPermissionService iSysPermissionService;

    /**
     * 角色列表select
     */
    @PostMapping("/select")
    @ApiOperation(value = "角色列表select")
    public RestResult<List<SysRoleSelectVO>> roleSelect() {
        List<SysRoleSelectVO> roleVOList = roleService.roleSelect();
        return RestResult.ok(roleVOList);
    }

    /**
     * 列表分页
     */
    @PostMapping("/listPage")
    @ApiOperation(value = "角色列表分页")
    public RestResult<List<SysRoleVO>> listPage(RoleListPageReq req) {
        PageReturn<SysRoleVO> roleVOAPage = roleService.listPage(req);
        return RestResult.page(roleVOAPage);
    }

    /**
     * 创建角色
     */
    @PostMapping
    @ApiOperation(value = "创建角色")
    @SysLog(title = "角色控制器",content = "创建角色")
    public RestResult createRole(@Validated @RequestBody SaveSysRoleReq req) {
        SysRole sysRole = roleService.createRole(req);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok(sysRole);
    }

    /**
     * 更新角色
     */
    @PutMapping
    @ApiOperation(value = "更新角色")
    @SysLog(title = "角色控制器",content = "更新角色")
    public RestResult updateRole(@Validated @RequestBody SaveSysRoleReq req) {
         SysRole sysRole = roleService.updateRole(req);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok(sysRole);
    }

    /**
     * 更新角色状态
     */
    @PatchMapping("/updateStatus/{id}/{status}")
    @ApiOperation(value = "更新角色状态")
    @SysLog(title = "角色控制器",content = "更新角色状态")
    public RestResult updateRole(@PathVariable Long id, @PathVariable int status) {
        SysRole sysRole = roleService.updateStatus(id, status);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok(sysRole);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{ids}")
    @ApiOperation(value = "删除角色")
    @SysLog(title = "角色控制器",content = "删除角色")
    public RestResult deletes(@PathVariable List<Long> ids) {

        roleService.deletes(ids);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok();
    }

}
