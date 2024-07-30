package org.uu.manager.service;



import com.baomidou.mybatisplus.extension.service.IService;

import org.uu.common.core.page.PageReturn;


import org.uu.common.pay.dto.UserAuthDTO;
import org.uu.common.pay.req.MerchantInfoPwdReq;
import org.uu.manager.entity.SysUser;
import org.uu.manager.req.SaveUserReq;
import org.uu.manager.req.UserListPageReq;
import org.uu.manager.vo.SysUserVO;

import java.util.List;


public interface ISysUserService extends IService<SysUser> {


    /**
     * 根据用户名获取认证用户信息，携带角色和密码
     *
     * @param username
     * @return
     */
    UserAuthDTO getByUsername(String username);

    /**
     * 创建用户
     * @param req
     */
    SysUser createUser(SaveUserReq req);

    /**
     * 用户详情信息
     * @param userId
     * @return
     */
    SysUserVO userDetail(Long userId);

    /**
     * 更新用户信息
     * @param req
     * @param userId
     */
    void updateUserInfo(SaveUserReq req, Long userId);

    /**
     * 批量删除用户
     * @param userIds
     */
    void mulDeleteUsers(List<Long> userIds);

    /**
     * 用户列表分页
     * @param req
     */
    PageReturn<SysUserVO> listPage(UserListPageReq req);

    /**
     * 更新用户状态
     * @param userId
     * @param status
     */
    SysUser updateStatus(Long userId, Integer status);

    /**
     * 获取当前用户信息
     * @return
     */
    SysUserVO currentUserInfo();

    void updateUserPwd(MerchantInfoPwdReq merchantInfoPwdReq);

    boolean validGoogle(String totpCode, String type);

    boolean resetGoogle(String userId);
}
