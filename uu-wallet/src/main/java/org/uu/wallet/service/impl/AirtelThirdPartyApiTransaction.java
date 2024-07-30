package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.wallet.req.KycSendOtpReq;
import org.uu.wallet.req.KycVerifyOtpReq;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;


/**
 * @author lukas
 */
@Service("airtelThirdPartyApi")
@RequiredArgsConstructor
@Slf4j
public class AirtelThirdPartyApiTransaction extends KycThirdPartyAbstract implements IKycThirdPartyTransaction {
    @Override
    public KycRestResult<String> sendOtp(KycSendOtpReq req) throws MalformedURLException {
        sendOtpCode(req.getPhoneNumber(), req.getBankCode());
        return KycRestResult.ok();
    }

    @Override
    public KycRestResult<String> verifyOtp(KycVerifyOtpReq req) throws MalformedURLException {
        String otp = req.getOtp();
        String phoneNumber = req.getPhoneNumber();
        JSONObject params = new JSONObject();
        params.put("phonenumber", phoneNumber);
        params.put("otp", otp);
        String result = sendVerifyCode(req.getBankCode(), JSONObject.toJSONString(params));
        if(ObjectUtils.isEmpty(result)){
            return KycRestResult.failed(ResultCode.OTP_INVALID);
        }
        return KycRestResult.ok(result);
    }
}
