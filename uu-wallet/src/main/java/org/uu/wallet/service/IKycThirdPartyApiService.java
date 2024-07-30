package org.uu.wallet.service;

import org.uu.common.core.result.KycRestResult;
import org.uu.wallet.req.KycSendOtpReq;
import org.uu.wallet.req.KycVerifyOtpReq;

import java.net.MalformedURLException;

/**
 * @author lukas
 */
public interface IKycThirdPartyApiService {

    /**
     * 发送短信
     * @param req {@link KycSendOtpReq}
     * @return {@link KycRestResult}
     * @throws MalformedURLException ex
     */
    KycRestResult<String>  sendOtp(KycSendOtpReq req) throws Exception;

    /**
     * 校验短信
     * @param req {@link KycVerifyOtpReq}
     * @return {@link KycRestResult}
     * @throws MalformedURLException ex
     */
    KycRestResult<String>  verifyOtp(KycVerifyOtpReq req) throws MalformedURLException;
}
