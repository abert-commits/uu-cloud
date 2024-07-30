package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.wallet.entity.UserVerificationCodes;
import org.uu.common.pay.req.UserTextMessageReq;

/**
 * <p>
 * 用户验证码记录表 服务类
 * </p>
 *
 * @author
 * @since 2024-01-20
 */
public interface IUserVerificationCodesService extends IService<UserVerificationCodes> {

    PageReturn<UserVerificationCodeslistPageDTO> listPage(UserTextMessageReq userTextMessageReq);
}
