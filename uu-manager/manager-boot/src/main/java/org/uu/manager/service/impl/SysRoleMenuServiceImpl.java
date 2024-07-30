package org.uu.manager.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.manager.entity.SysRoleMenu;
import org.uu.manager.mapper.SysRoleMenuMapper;
import org.uu.manager.service.ISysRoleMenuService;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {

}
