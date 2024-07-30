package org.uu.manager.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.manager.config.AdminConfig;
import org.uu.manager.config.AdminMapStruct;
import org.uu.manager.entity.SysPermission;
import org.uu.manager.entity.SysRoleMenu;
import org.uu.manager.entity.SysRolePermission;
import org.uu.manager.mapper.SysPermissionMapper;
import org.uu.manager.req.CommonReq;
import org.uu.manager.req.SavePermissionReq;
import org.uu.manager.service.ISysPermissionService;
import org.uu.manager.service.ISysRolePermissionService;
import org.uu.manager.vo.SysPermissionVO;
import org.uu.manager.vo.SysServiceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final RedisTemplate redisTemplate;
    private final AdminMapStruct adminMapStruct;
    private final AdminConfig adminConfig;
    private final ISysRolePermissionService rolePermissionService;


    @Override
    public SysPermission updatePermission(SavePermissionReq req) {
        AssertUtil.notEmpty(req.getId(), ResultCode.PARAM_VALID_FAIL);
        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(req, sysPermission);
        String urlPerm = String.format(GlobalConstants.ADMIN_URL_PERM, req.getMethod(), req.getServiceName(), req.getUrl());
        sysPermission.setUrlPerm(urlPerm);
        updateById(sysPermission);
        return sysPermission;
    }

    @Override
    public SysPermission createPermission(SavePermissionReq req) {
        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(req, sysPermission);
        String urlPerm = String.format(GlobalConstants.ADMIN_URL_PERM, req.getMethod(), req.getServiceName(), req.getUrl());
        sysPermission.setUrlPerm(urlPerm);
        save(sysPermission);
        return sysPermission;
    }

    @Override
    public List<SysPermissionVO> listByMenuId(Long menuId) {
        List<SysPermission> sysPermissions = lambdaQuery().eq(SysPermission::getMenuId, menuId).list();
        List<SysPermissionVO> sysPermissionVOList = new ArrayList<SysPermissionVO>() ;
        if (CollectionUtil.isNotEmpty(sysPermissions)) {
            for(SysPermission sysPermission : sysPermissions){

                SysPermissionVO sysPermissionVO = new SysPermissionVO();
                if(!StringUtils.isEmpty(sysPermission.getUrlPerm())) {
                    BeanUtils.copyProperties(sysPermission, sysPermissionVO);
                    String[] a = sysPermission.getUrlPerm().split("/");
                    sysPermissionVO.setMethod(a[0].substring(0,a[0].indexOf(":")));
                    sysPermissionVO.setServiceName(a[1]);
                    sysPermissionVO.setUrl(sysPermission.getUrlPerm().substring(sysPermission.getUrlPerm().indexOf(a[1])+a[1].length()));
                    sysPermissionVOList.add(sysPermissionVO);
                }else{
                    sysPermissionVOList.add(sysPermissionVO);
                }
            }
            return sysPermissionVOList;
            //return adminMapStruct.sysPermissionToSysPermissionVO(sysPermissions);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean refreshPermRolesRules() {
        redisTemplate.delete(Arrays.asList(GlobalConstants.URL_PERM_ROLES_KEY));
        List<SysPermission> permissions = this.listPermRoles();
        if (CollectionUtil.isNotEmpty(permissions)) {
            //
            List<SysPermission> urlPermList = permissions.stream()
                    .filter(item -> StrUtil.isNotBlank(item.getUrlPerm()))
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(urlPermList)) {
                Map<String, List<String>> urlPermRoles = new HashMap<>();
                urlPermList.stream().forEach(item -> {
                    String perm = item.getUrlPerm();
                    List<String> roles = item.getRoles();
                    urlPermRoles.put(perm, roles);
                });
                redisTemplate.opsForHash().putAll(GlobalConstants.URL_PERM_ROLES_KEY, urlPermRoles);
            }
        }
        return true;
    }

    @Override
    public List<SysPermission> listPermRoles() {
        return this.baseMapper.listPermRoles();
    }

    @Override
    public List<SysServiceVO> getServices() {
        List<String> services = adminConfig.getServices();
        if (CollectionUtil.isNotEmpty(services)) {
            List<String> validServices = services.stream().filter(service -> service.split(",").length == 2).collect(Collectors.toList());
            return validServices.stream().map(service -> {
                String[] nameCode = service.split(",");
                SysServiceVO sysServiceVO = new SysServiceVO();
                sysServiceVO.setServiceCode(nameCode[0]);
                sysServiceVO.setServiceName(nameCode[1]);
                return sysServiceVO;
            }).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void deletePermissions(List<Long> ids) {
        this.baseMapper.deleteBatchIds(ids);
    }

    @Override
    public List<Long> listRolePermission(Long roleId) {
        List<SysRolePermission> list = rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId).list();
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SysPermission> updateRolePermission(Long roleId, CommonReq req) {

        rolePermissionService.getBaseMapper().delete(rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId).getWrapper());
        List<SysRolePermission> roleMenus = new ArrayList<SysRolePermission>();
        if (CollectionUtil.isNotEmpty(req.getIds())) {
             roleMenus = req.getIds().stream().map(menuId -> new SysRolePermission(menuId, roleId)).collect(Collectors.toList());
            rolePermissionService.saveBatch(roleMenus);
        }
        List<Long> permIds = roleMenus.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
        List<SysPermission> list = this.lambdaQuery().in(SysPermission::getId, permIds).list();
        return list;
    }

}
