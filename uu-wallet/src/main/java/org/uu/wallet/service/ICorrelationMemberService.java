package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CorrelationMemberDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.wallet.entity.CorrelationMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 关联会员信息 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-30
 */
public interface ICorrelationMemberService extends IService<CorrelationMember> {

    PageReturn<CorrelationMemberDTO> listPage(MemberBlackReq req);
}
