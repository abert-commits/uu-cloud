package org.uu.manager.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.manager.entity.SysRolePermission;
import org.uu.manager.mapper.SysRolePermissionMapper;
import org.uu.manager.service.ISysRolePermissionService;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {


}
