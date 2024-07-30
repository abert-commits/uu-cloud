package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.InviteCodeInfoDTO;
import org.uu.common.pay.req.InviteCodeInfoReq;
import org.uu.common.pay.vo.request.InviteLinkSaveRequestVO;
import org.uu.common.pay.vo.response.InviteInfoDetailResponseVO;
import org.uu.common.pay.vo.response.InviteInfoResponseVO;
import org.uu.wallet.entity.InviteLink;

import java.util.List;

/**
 * <p>
 * 邀请链接表 服务类
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
public interface InviteLinkService extends IService<InviteLink> {
    /**
     * 邀请链接列表
     */
    RestResult<PageReturn<InviteInfoDetailResponseVO>> inviteLinkList(PageRequestHome pageRequest);

    /**
     * 删除邀请链接
     * @param id 邀请链接ID
     * @return {@link RestResult<Void>}
     */
    RestResult<Void> removeInviteLink(Long id);

    /**
     * 将邀请链接设置为默认
     * @param id 邀请链接ID
     * @return {@link RestResult<Void>}
     */
    RestResult<Void> setDefaultInviteLink(Long id);

    /**
     * 添加邀请链接
     * @param requestVO 请求实体类
     * @return {@link RestResult<Void>}
     */
    RestResult<Void> saveInviteLink(InviteLinkSaveRequestVO requestVO);

    /**
     * 根据会员ID查询会员邀请码信息
     * @param requestVO 请求实体类
     */
    RestResult<PageReturn<InviteCodeInfoDTO>> inviteCodeList(InviteCodeInfoReq requestVO);

    String getDefaultInviteCode(Long currentUserId);
}
