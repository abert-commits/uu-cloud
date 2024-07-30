package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.vo.request.MemberNotificationListRequestVO;
import org.uu.common.pay.vo.response.MemberNotificationResponseVO;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberNotification;
import org.uu.wallet.req.BindEmailReq;
import org.uu.wallet.req.TradeNotificationReq;
import org.uu.wallet.req.VerifySmsCodeReq;

/**
 * @author
 */
public interface IMemberNotificationService extends IService<MemberNotification> {

    /**
     * 根据会员id获取通知数量
     *
     * @param memberId
     * @return {@link Integer}
     */
    Integer getNotificationCountByMemberId(String memberId);

    /**
     * 校验短信验证码
     *
     * @param verifySmsCodeReq
     * @return {@link Boolean}
     */
    Boolean validateSmsCode(VerifySmsCodeReq verifySmsCodeReq);

    /**
     * 校验邮箱验证码
     *
     * @param bindEmailReq
     * @param memberInfo
     * @return {@link Boolean}
     */
    Boolean validateEmailCode(BindEmailReq bindEmailReq, MemberInfo memberInfo);

    /**
     * 获取消息通知列表
     * @param requestVO 请求参数
     */
    RestResult<PageReturn<MemberNotificationResponseVO>> memberNotificationList(MemberNotificationListRequestVO requestVO);

    /**
     * 一键已读
     * @return
     */
    RestResult<Void> allRead();

    /**
     * 根据ID读取消息
     * @param id 消息ID
     */
    RestResult<Void> readById(Long id);

    /**
     * 新增一条消息通知记录
     * @param requestVO 请求实体类
     */
    Boolean insertPayNotification(TradeNotificationReq requestVO);
}


