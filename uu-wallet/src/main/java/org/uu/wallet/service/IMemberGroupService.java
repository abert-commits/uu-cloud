package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MemberGroupListPageDTO;
import org.uu.common.pay.req.MemberGroupListPageReq;
import org.uu.wallet.entity.MemberGroup;
import org.uu.wallet.entity.MemberInfo;

/**
* @author
*/
    public interface IMemberGroupService extends IService<MemberGroup> {

     PageReturn<MemberGroupListPageDTO> listPage(MemberGroupListPageReq req);


    /**
     * 根据会员的交易数据 进行会员分组
     *
     * @param memberInfo
     * @return {@link MemberInfo}
     */
    MemberInfo determineMemberGroup(MemberInfo memberInfo);


    /**
     * 根据分组id获取权限列表
     *
     * @param id
     * @return {@link String}
     */
    String getAuthListById(Long id);

}
