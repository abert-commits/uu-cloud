package org.uu.manager.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.GoogleAuthenticatorUtil;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.pay.dto.UserAuthDTO;
import org.uu.common.pay.req.MerchantInfoPwdReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.api.MerchantInfoClient;
import org.uu.manager.config.AdminMapStruct;
import org.uu.manager.entity.*;
import org.uu.manager.mapper.SysUserMapper;
import org.uu.manager.req.SaveUserReq;
import org.uu.manager.req.UserListPageReq;
import org.uu.manager.service.*;
import org.uu.manager.util.PageUtils;
import org.uu.manager.vo.SysUserVO;
import org.uu.manager.websocket.SendForbidUserMsg;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final PasswordEncoder passwordEncoder;
    private final ISysUserRoleService userRoleService;
    private final ISysRoleMenuService roleMenuService;
    private final ISysRolePermissionService rolePermissionService;
    private final ISysPermissionService permissionService;
    private final AdminMapStruct adminMapStruct;
    private final ISysMenuService sysMenuService;
    private final ISysRoleService roleService;
    private final SysUserMapper sysUserMapper;
    private final MerchantInfoClient merchantInfoClient;
    private final RedisUtils redisUtils;
    private final SendForbidUserMsg sendForbidUserMsg;


    @Override
    public UserAuthDTO getByUsername(String username) {
        String lastLoginIp = (String) redisUtils.hget(SecurityConstants.LOGIN_USER_NAME + username, SecurityConstants.LOGIN_LAST_LOGIN_IP);
        Integer loginCount = (Integer) redisUtils.hget(SecurityConstants.LOGIN_USER_NAME + username, SecurityConstants.LOGIN_COUNT);
        UserAuthDTO userAuthInfo = this.baseMapper.getByUsername(username);
        if(ObjectUtils.isNotEmpty(userAuthInfo)){
            this.baseMapper.updateUserInfo(lastLoginIp, userAuthInfo.getUserId(), loginCount);
        }
        return userAuthInfo;
    }


    @Override
    public SysUser createUser(SaveUserReq req) {
        // 生成密码
        String passwd = passwordEncoder.encode(GlobalConstants.USER_DEFAULT_PASSWORD);
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(req, sysUser);
        sysUser.setPassword(passwd);
        sysUser.setGooglesecret(GoogleAuthenticatorUtil.getSecretKey());
        save(sysUser);
        // 维护角色关系
        saveUserRoles(req.getRoleIds(), sysUser.getId());
        return sysUser;
    }

    private void saveUserRoles(List<Long> roleIds, Long userId) {
        if (CollectionUtil.isNotEmpty(roleIds)) {
            List<SysUserRole> sysUserRoles = new ArrayList<>();
            roleIds.forEach(roleId -> {
                sysUserRoles.add(new SysUserRole(userId, roleId));
            });
            userRoleService.saveBatch(sysUserRoles);
        }
    }

    @Override
    public SysUserVO userDetail(Long userId) {
        SysUser sysUser = lambdaQuery().eq(SysUser::getId, userId).one();
        String passwd = passwordEncoder.encode(GlobalConstants.USER_DEFAULT_PASSWORD);
        AssertUtil.notEmpty(sysUser, ResultCode.USERNAME_OR_PASSWORD_ERROR);
        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);
        // 查询绑定的角色IDs
        List<Long> roleIds = userRoleService.selectRoleIds(userId);
        sysUserVO.setRoleIds(roleIds);
        return sysUserVO;
    }

    @Override
    public void updateUserInfo(SaveUserReq req, Long userId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(req, sysUser);
        lambdaUpdate().eq(SysUser::getId, userId).update(sysUser);
        // 维护角色列表
        userRoleService.deleteByUserId(userId);
        saveUserRoles(req.getRoleIds(), userId);
        if(sysUser.getStatus().compareTo(0)==0){
            String jti =  (String)redisUtils.get(SecurityConstants.LOGIN_USER_ID + sysUser.getUsername());
            log.info(sysUser.getUsername()+"对应的jti->{}", jti);
            if(StringUtils.isEmpty(jti)) return;
            CommonUtils.deleteToken(sysUser.getUsername(), redisUtils);
            redisUtils.set(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti, null, 1000);
            sendForbidUserMsg.send("You are temporarily unable to use the system","0",sysUser.getId().toString());
        }
    }

    @Override
    public void mulDeleteUsers(List<Long> userIds) {
        // 删除用户信息
        lambdaUpdate().in(SysUser::getId, userIds).set(SysUser::getDeleted, GlobalConstants.STATUS_ON).update();
        // 删除用户关联的角色
        userRoleService.getBaseMapper().delete(userRoleService.lambdaQuery().in(SysUserRole::getUserId, userIds).getWrapper());
    }

    @Override
    public PageReturn<SysUserVO> listPage(UserListPageReq req) {
        Page<SysUser> page = new Page<>();
        page.setCurrent(1);
        page.setSize(1000);
        LambdaQueryChainWrapper<SysUser> lambdaQuery = lambdaQuery();
        //lambdaQuery.eq(SysUser::getStatus, GlobalConstants.STATUS_ON);
        lambdaQuery.eq(SysUser::getDeleted, GlobalConstants.STATUS_OFF);
        if (!StringUtils.isEmpty(req.getKeyword())) {
            lambdaQuery.like(SysUser::getUsername, req.getKeyword()).or().like(SysUser::getNickname, req.getKeyword());
        }
        if (!StringUtils.isEmpty(req.getUsername())) {
            lambdaQuery.eq(SysUser::getUsername, req.getUsername());
        }

        if (!StringUtils.isEmpty(req.getRole())) {
                List<SysRole> sysRoleList = roleService.lambdaQuery().eq(SysRole::getName,req.getRole()).list();
            if(sysRoleList!=null&&sysRoleList.size()>0){
                List<Long> idList=sysRoleList.stream().map(SysRole::getId).collect(Collectors.toList());
                List<SysUserRole> listUserRole = userRoleService.lambdaQuery().in(SysUserRole::getRoleId,idList).list();
                List<Long> listUserId =listUserRole.stream().map(SysUserRole::getUserId).collect(Collectors.toList());
                lambdaQuery().in(SysUser::getId,listUserId);
            }

        }
        lambdaQuery.orderByDesc(SysUser::getId);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<SysUser> records = page.getRecords();
        List<SysUserVO> sysUserVOS = adminMapStruct.sysUserToSysUserVO(records);
        List<SysUserVO> sysUserList = new ArrayList<>();
        for(SysUserVO sysUserVO : sysUserVOS) {
            List<Long> roleIds = userRoleService.selectRoleIds(sysUserVO.getId());
            if(roleIds==null||roleIds.size()<1) continue;
            List<SysRoleMenu> roleMenus = roleMenuService.lambdaQuery().in(SysRoleMenu::getRoleId, roleIds).list();
            List<SysRole> listrole = roleService.lambdaQuery().in(SysRole::getId, roleIds).list();
            for (Long item : roleIds) {
                if(org.apache.commons.lang3.StringUtils.isNotBlank(req.getRole()) && item == Long.parseLong(req.getRole())){
                    sysUserList.add(sysUserVO);
                }
            }
            sysUserVO.setListRole(listrole);
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(req.getRole())){
            page.setTotal(sysUserList.size());
            List<SysUserVO> tmp = sysUserList.stream().skip((req.getPageNo()-1)*req.getPageSize()).limit(req.getPageSize()).
                    collect(Collectors.toList());
            return PageUtils.flush(page, tmp);
        }else {
            page.setTotal(sysUserVOS.size());
            List<SysUserVO> tmp = sysUserVOS.stream().skip((req.getPageNo()-1)*req.getPageSize()).limit(req.getPageSize()).
                    collect(Collectors.toList());
            return PageUtils.flush(page, tmp);
        }

    }

    @Override
    public SysUser updateStatus(Long userId, Integer status) {
        lambdaUpdate().set(SysUser::getStatus, status).eq(SysUser::getId, userId).update();
        SysUser sysUser =this.lambdaQuery().eq(SysUser::getId, userId).one();
        if(status.compareTo(0)==0){
            String jti =  (String)redisUtils.get(SecurityConstants.LOGIN_USER_ID + sysUser.getUsername());
            if(StringUtils.isEmpty(jti)) return sysUser;
            CommonUtils.deleteToken(sysUser.getUsername(), redisUtils);
            redisUtils.set(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti, null, 1000);
            sendForbidUserMsg.send("You are temporarily unable to use the system","0",sysUser.getId().toString());
        }
        return sysUser;
    }

    @Override
    public SysUserVO currentUserInfo() {
        Long currentUserId = UserContext.getCurrentUserId();
        AssertUtil.notEmpty(currentUserId, ResultCode.RELOGIN);
        SysUserVO sysUserVO = userDetail(currentUserId);
        AssertUtil.notEmpty(sysUserVO, ResultCode.USERNAME_OR_PASSWORD_ERROR);
        // 查询绑定的菜单
        List<Long> roleIds = userRoleService.selectRoleIds(currentUserId);
        List<SysRoleMenu> roleMenus = roleMenuService.lambdaQuery().in(SysRoleMenu::getRoleId, roleIds).list();
        List<SysRole> listrole = roleService.lambdaQuery().in(SysRole::getId,roleIds).list();
        sysUserVO.setListRole(listrole);

        sysUserVO.setMenuIds(roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
        List<SysMenu> listMenu = sysMenuService.lambdaQuery().in(SysMenu::getId,sysUserVO.getMenuIds()).list();
        sysUserVO.setListMenu(listMenu);
        // 获取权限
        List<SysRolePermission> rolePermissions = rolePermissionService.lambdaQuery().in(SysRolePermission::getRoleId, roleIds).list();
        sysUserVO.setPermissions(Arrays.asList());
        if (CollectionUtil.isNotEmpty(rolePermissions)) {
            List<Long> permissionIds = rolePermissions.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
            List<SysPermission> sysPermissions = permissionService.lambdaQuery().in(SysPermission::getId, permissionIds).list();
            if (CollectionUtil.isNotEmpty(sysPermissions)) {
                List<String> btnSigns = sysPermissions.stream().map(SysPermission::getBtnSign).collect(Collectors.toList());
                sysUserVO.setPermissions(btnSigns);
            }
        }

        return sysUserVO;
    }

    @Override
    public void updateUserPwd(MerchantInfoPwdReq merchantInfoPwdReq) {

        if(!merchantInfoPwdReq.getNewPwd().equals(merchantInfoPwdReq.getConfirmNewPwd())){
            throw new BizException(ResultCode.MERCHANT_PASSWORDS_INCONSISTENT);
        }

        // 校验原始密码是否正确
//        SysUserVO sysUser = userDetail(merchantInfoPwdReq.getId());
//        boolean result = passwordEncoder.matches(merchantInfoPwdReq.getOriginalPwd(), sysUser.getPassword());
//        if(!result){
//            throw new BizException(ResultCode.MERCHANT_ORIGINAL_PASSWORDS_WRONG);
//        }
        String newPwd = passwordEncoder.encode(merchantInfoPwdReq.getNewPwd());
        sysUserMapper.updateUserPwd(merchantInfoPwdReq.getId(), newPwd);

    }

    @Override
    public boolean validGoogle(String totpCode, String type) {

        if(type.equals("1")){
            Long currentUserId = UserContext.getCurrentUserId();
            SysUser sysUser = this.lambdaQuery().eq(SysUser::getId, currentUserId).one();
             boolean result = GoogleAuthenticatorUtil.checkCode(sysUser.getGooglesecret(),Long.parseLong(totpCode), System.currentTimeMillis());
            if(result){
                // 更新用户谷歌绑定标识
                sysUserMapper.updateUserGoogelBindFlag(currentUserId, 1);
            }
            return result;
        }else {
             RestResult<Boolean> result = merchantInfoClient.validGoogle(totpCode);
             return result.getData();
        }

    }

    @Override
    public boolean resetGoogle(String userId) {
        String newGoogleSecretKey = GoogleAuthenticatorUtil.getSecretKey();
        return sysUserMapper.updateUserGoogleSecretKey(Long.parseLong(userId), newGoogleSecretKey, 0) > 0;
    }
}
