package org.uu.manager.controller;


import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;


import org.uu.common.core.utils.CommonUtils;
import org.uu.common.pay.dto.UserAuthDTO;
import org.uu.common.pay.req.MerchantInfoPwdReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MerchantInfoClient;
import org.uu.manager.entity.SysUser;
import org.uu.manager.req.SaveUserReq;
import org.uu.manager.req.UserListPageReq;

import org.uu.manager.service.ISysPermissionService;
import org.uu.manager.service.ISysUserService;
import org.uu.manager.vo.SysUserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping(value = {"/api/v1/users", "/user"})
@Slf4j
@RequiredArgsConstructor
@Api(description = "用户控制器")
public class UserController {

    private final ISysUserService sysUserService;
    private final RedisUtils redisUtils;
    private final ISysPermissionService iSysPermissionService;






    /**
     * 获取用户信息
     */
    @GetMapping("/username/{username}")
    @ApiOperation(value = "获取用户信息")
    public RestResult<UserAuthDTO> getUserByUsername(@NotBlank(message = "username 不能为空") @PathVariable String username) {
        UserAuthDTO user = sysUserService.getByUsername(username);
        return RestResult.ok(user);
    }

    /**
     * 获取当前用户信息
     */
    @PostMapping("/current")
    @ApiOperation(value = "获取当前用户信息")
    public RestResult<SysUserVO> currentUserInfo() {
        SysUserVO sysUserVO = sysUserService.currentUserInfo();
        return RestResult.ok(sysUserVO);
    }

    /**
     * 校验谷歌验证码
     */
    @PostMapping("/validGoogle")
    @ApiOperation(value = "校验谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "totpCode", value = "谷歌验证码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "后台类型: 1-管理后台 2-商户后台", required = true, dataType = "String")
    })
    public RestResult validGoogle(@RequestParam(value = "totpCode") String totpCode, @RequestParam(value = "type") String type) {
        boolean result = sysUserService.validGoogle(totpCode, type);
        return result ? RestResult.ok() : RestResult.failed("Verification code error");
    }


    /**
     * 重置谷歌谷歌验证码
     */
    @PostMapping("/resetGoogle")
    @ApiOperation(value = "重置谷歌密钥")
    public RestResult resetGoogle(@RequestParam(value = "userId") String userId) {
        boolean result = sysUserService.resetGoogle(userId);
        return result ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 创建用户
     */
    @PostMapping
    @SysLog(title = "用户控制器",content = "创建用户")
    @ApiOperation(value = "创建用户")
    public RestResult createUser(@Validated @RequestBody SaveUserReq req) {
      SysUser sysUser =  sysUserService.createUser(req);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok(sysUser);
    }

    /**
     * 用户详情
     */
    @PostMapping("/{userId}")
    @ApiOperation(value = "用户详情")
    public RestResult<SysUserVO> userDetail(@NotNull(message = "userId 不能为空") @PathVariable Long userId) {
        SysUserVO sysUserVO = sysUserService.userDetail(userId);
        return RestResult.ok(sysUserVO);
    }

    /**
     * 更新用户信息
     */

    @PutMapping("/{userId}")
    @SysLog(title = "用户控制器",content = "更新用户信息")
    @ApiOperation(value = "更新用户信息")
    public RestResult updateUserInfo(@Validated @RequestBody SaveUserReq req, @NotNull(message = "userId 不能为空") @PathVariable Long userId) {
        sysUserService.updateUserInfo(req, userId);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok();
    }

    @PostMapping("/updateUserPwd")
    @SysLog(title = "用户控制器",content = "修改用户密码")
    @ApiOperation(value = "修改用户密码")
    public RestResult updateUserPwd(@RequestBody @ApiParam MerchantInfoPwdReq merchantInfoPwdReq) {
        sysUserService.updateUserPwd(merchantInfoPwdReq);
        return RestResult.ok();
    }

    /**
     * 批量删除用户
     */

    @DeleteMapping("/{userIds}")
    @SysLog(title = "用户控制器",content = "批量删除用户")
    @ApiOperation(value = "批量删除用户")
    public RestResult mulDeleteUsers(@Size(min = 1, message = "userIds 不能为空") @PathVariable List<Long> userIds) {
        sysUserService.mulDeleteUsers(userIds);
        return RestResult.ok();
    }

    /**
     * 用户分页列表
     */

    @PostMapping("/listPage")
    @ApiOperation(value = "用户分页列表")
    public RestResult<List<SysUserVO>> listPage(@RequestBody UserListPageReq req) {
        PageReturn<SysUserVO> sysUserVOPage = sysUserService.listPage(req);
        return RestResult.page(sysUserVOPage);
    }

    /**
     * 更新用户状态
     */

    @PatchMapping("/updateStatus/{userId}/{status}")
    @SysLog(title = "用户控制器",content = "更新用户状态")
    @ApiOperation(value = "更新用户状态")
    public RestResult updateStatus(@NotNull(message = "userId 不能为空") @PathVariable Long userId, @NotNull(message = "status 不能为空") @PathVariable Integer status) {
        SysUser sysUser = sysUserService.updateStatus(userId, status);
        iSysPermissionService.refreshPermRolesRules();
        return RestResult.ok(sysUser);
    }


    @PostMapping("/online")
    @ApiOperation(value = "获取在线人数")
    public RestResult getOnLineCount() {
        Long onLineCount = CommonUtils.getOnlineCount(redisUtils);
        return RestResult.ok(onLineCount);
    }



}
