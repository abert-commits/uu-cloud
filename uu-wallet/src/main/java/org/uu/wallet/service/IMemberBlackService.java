package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberBlackDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.wallet.entity.MemberBlack;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 会员黑名单 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
public interface IMemberBlackService extends IService<MemberBlack> {

    PageReturn<MemberBlackDTO> listPage(MemberBlackReq req);

    RestResult removeBlack(MemberBlackReq req);

    Boolean addBlack(MemberBlack req);
}
