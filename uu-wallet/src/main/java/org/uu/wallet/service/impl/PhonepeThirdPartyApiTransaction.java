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
@Service("phonepeThirdPartyApi")
@RequiredArgsConstructor
@Slf4j
public class PhonepeThirdPartyApiTransaction extends KycThirdPartyAbstract implements IKycThirdPartyTransaction {
    @Override
    public KycRestResult<String> sendOtp(KycSendOtpReq req) throws MalformedURLException {
        String result = sendOtpCode(req.getPhoneNumber(), req.getBankCode());
        if(ObjectUtils.isEmpty(result)){
            return KycRestResult.failed(ResultCode.KYC_API_REQUEST_FAILED);
        }
        return KycRestResult.ok(result);
    }

    @Override
    public KycRestResult<String> verifyOtp(KycVerifyOtpReq req) throws MalformedURLException {
        String otp = req.getOtp();
        String phoneNumber = req.getPhoneNumber();
        JSONObject params = new JSONObject();
        params.put("phonenumber", phoneNumber);
        params.put("otp", otp);
        // 获取上个接口返回的参数信息
        String response = req.getResponse();
        JSONObject responseObj = JSONObject.parseObject(response);
        if( !(responseObj.containsKey("sendOtp_response")
                && responseObj.containsKey("sendOtp_response1")
                && responseObj.containsKey("device_id"))
        ){
            return KycRestResult.failed("Parameter missing: " + response);
        }
        params.put("sendOtp_response", responseObj.getString("sendOtp_response"));
        params.put("sendOtp_response1", responseObj.getString("sendOtp_response1"));
        params.put("device_id", responseObj.getString("device_id"));
        String result = sendVerifyCode(req.getBankCode(), JSONObject.toJSONString(params));
        if(ObjectUtils.isEmpty(result)){
            return KycRestResult.failed(ResultCode.OTP_INVALID);
        }
        return KycRestResult.ok(result);
    }
}
