package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.entity.SysPermission;
import org.uu.manager.req.CommonReq;
import org.uu.manager.req.SavePermissionReq;
import org.uu.manager.service.ISysPermissionService;
import org.uu.manager.vo.SysPermissionVO;
import org.uu.manager.vo.SysServiceVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/permission")
@Slf4j
@RequiredArgsConstructor
@Api(description = "权限控制器")
public class PermissionController {
    private final ISysPermissionService permissionService;

    /**
     * 权限列表
     */
    @PostMapping("/list/{menuId}")
    @ApiOperation(value = "获取权限列表")
    public RestResult<List<SysPermissionVO>> list(@PathVariable Long menuId) {
        List<SysPermissionVO> permissionVOS = permissionService.listByMenuId(menuId);
        return RestResult.ok(permissionVOS);
    }

    /**
     * 创建权限
     */
    @PostMapping
    @ApiOperation(value = "创建权限")
    @SysLog(title = "权限控制器",content = "创建权限")
    public RestResult createPermission(@Validated @RequestBody SavePermissionReq req) {
        SysPermission sysPermission = permissionService.createPermission(req);
        permissionService.refreshPermRolesRules();
        return RestResult.ok(sysPermission);
    }

    /**
     * 更新权限
     */
    @PutMapping
    @ApiOperation(value = "更新权限")
    @SysLog(title = "权限控制器",content = "更新权限")
    public RestResult updatePermission(@Validated @RequestBody SavePermissionReq req) {
        SysPermission sysPermission = permissionService.updatePermission(req);
        permissionService.refreshPermRolesRules();
        return RestResult.ok(sysPermission);
    }

    /**
     * 获取服务列表
     */
    @PostMapping("/services")
    @ApiOperation(value = "获取服务列表")
    public RestResult<List<SysServiceVO>> getServices() {
        List<SysServiceVO> sysServiceVOS = permissionService.getServices();
        return RestResult.ok(sysServiceVOS);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{ids}")
    @ApiOperation(value = "删除权限")
    @SysLog(title = "权限控制器",content = "删除权限")
    public RestResult getServices(@PathVariable List<Long> ids) {
        permissionService.deletePermissions(ids);
        permissionService.refreshPermRolesRules();
        return RestResult.ok();
    }

    /**
     * 查询角色绑定的权限
     */
    @PostMapping("/role/{roleId}")
    @ApiOperation(value = "查询角色绑定的权限")
    public RestResult<List<Long>> listRolePermission(@PathVariable Long roleId) {
        List<Long> permissions = permissionService.listRolePermission(roleId);
        return RestResult.ok(permissions);
    }

    /**
     * 更新角色绑定的权限
     */
    @PutMapping("/role/{roleId}")
    @ApiOperation(value = "更新角色绑定的权限")
    @SysLog(title = "权限控制器",content = "更新角色绑定的权限")
    public RestResult updateRolePermission(@PathVariable Long roleId, @RequestBody CommonReq req) {
       List<SysPermission> list = permissionService.updateRolePermission(roleId, req);
        permissionService.refreshPermRolesRules();
        return RestResult.ok(list);
    }
}
