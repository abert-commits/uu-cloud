package org.uu.manager.config;


import org.uu.common.pay.dto.*;
import org.uu.manager.entity.SysPermission;
import org.uu.manager.entity.SysRole;
import org.uu.manager.entity.SysUser;
import org.uu.manager.vo.SysPermissionVO;
import org.uu.manager.vo.SysRoleSelectVO;
import org.uu.manager.vo.SysRoleVO;
import org.uu.manager.vo.SysUserVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapStruct {
    List<SysRoleSelectVO> sysRoleToSysRoleVO(List<SysRole> sysRoles);
    List<SysUserVO> sysUserToSysUserVO(List<SysUser> sysUsers);
    List<SysRoleVO> sysRoleToSysRoleListVO(List<SysRole> sysRoles);
    List<SysPermissionVO> sysPermissionToSysPermissionVO(List<SysPermission> permissions);
    List<RechargeOrderExportForMerchantDTO> toRechargeOrderExportForMerchantDTO(List<RechargeOrderExportDTO> dto);
    List<WithdrawOrderExportForMerchantDTO> toWithdrawOrderExportForMerchantDTO(List<WithdrawOrderExportDTO> dto);

    List<MemberAccountChangeExportForMerchantDTO> toMemberAccountChangeExportForMerchantDTO(List<MemberAccountChangeExportDTO> dto);
}
