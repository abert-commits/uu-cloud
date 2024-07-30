package org.uu.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.uu.common.pay.dto.MemberUserAuthDTO;
import org.uu.manager.entity.MemberUser;

public interface IMemberUserService extends IService<MemberUser> {


    /**
     * 根据用户名获取认证用户信息，携带角色和密码
     *
     * @param username
     * @return
     */
    MemberUserAuthDTO getByUsername(String username);

}
