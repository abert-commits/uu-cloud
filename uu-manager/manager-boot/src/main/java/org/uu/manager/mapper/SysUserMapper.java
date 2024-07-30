
package org.uu.manager.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.common.pay.dto.UserAuthDTO;


import org.uu.manager.entity.SysUser;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    UserAuthDTO getByUsername(@Param("userName") String userName);

    void updateUserPwd(@Param("id") Long userId, @Param("password") String password);

    Integer updateUserGoogleSecretKey(@Param("userId") Long userId, @Param("googlesecret") String googlesecret, @Param("flag")Integer flag);

    void updateUserGoogelBindFlag(@Param("id")Long id, @Param("flag")Integer flag);

    void updateUserInfo(@Param("lastLoginIp") String lastLoginIp, @Param("id")Long id, @Param("loginCount")Integer loginCount);

    @Select("select id from sys_manager.sys_user where deleted=0 and status =1")
    List<Long> getEffectiveUserIdList();
}

