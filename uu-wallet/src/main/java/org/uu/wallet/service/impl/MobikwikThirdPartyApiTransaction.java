package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.wallet.req.KycSendOtpReq;
import org.uu.wallet.req.KycVerifyOtpReq;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.util.RsaUtil;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.security.PublicKey;

import static com.alibaba.druid.filter.config.ConfigTools.getPublicKey;


/**
 * @author lukas
 */
@Service("mobikwikThirdPartyApi")
@RequiredArgsConstructor
@Slf4j
public class MobikwikThirdPartyApiTransaction extends KycThirdPartyAbstract implements IKycThirdPartyTransaction {

    @Override
    public KycRestResult<String> sendOtp(KycSendOtpReq req) throws Exception {
        String path = "/p/otp/v1/generate";
        String generateOtpRequestTimeId = sendOtpCode(getParam(req.getPhoneNumber()), req.getBankCode(), path);
        return ObjectUtils.isEmpty(generateOtpRequestTimeId) ? KycRestResult.failed(ResultCode.KYC_API_REQUEST_FAILED) : KycRestResult.ok(generateOtpRequestTimeId);
    }

    @Override
    public KycRestResult<String> verifyOtp(KycVerifyOtpReq req) throws MalformedURLException {
        String path = "/p/account/onboard/verify/v3";
        JSONObject object = new JSONObject();
        object.put("cell", req.getPhoneNumber());
        object.put("generateOtpRequestTimeId", req.getResponse());
        object.put("otp", req.getOtp());
        object.put("module", "LOGIN");
        object.put("v", "3");
        object.put("otpTime", System.currentTimeMillis());
        String token = sendVerifyCode(req.getBankCode(), JSONObject.toJSONString(object), path);
        if(ObjectUtils.isEmpty(token)){
            return KycRestResult.failed(ResultCode.OTP_INVALID);
        }
        return KycRestResult.ok(token);
    }

    private String getParam(String phoneNumber) throws Exception {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8ZQ1xafwImhvjVeJZmPVCXjG8k5A63j9t3NzEn0TIfzI0ZJCYJ87TQWXTG7c7ODu/lloyQ/gUMJw8hdCmG6J+mbuVRCBghg90Wecbp54lfOswH4xALJ1CBJN+1eAvvcjogadRMCSb0RuoJTSEZ6wzS23W46agvLIt8yhAo4Qkkpc2vSpvtgqZ7kz63Mp61Fl+1QnpNdpQx9RdDpNiuyH2q+I3/EMbDf8y4saEwKEjJKbfn/qZPjZeiownsKmAJ1LGTMwDeHPpJXTSGsku+I7I50fhBjODXQ5ArmpAtc8iz4rEpWuhtMV2R/a+1GZcbnqHBeOvDQhPAA9tlRn322+lwIDAQAB";
        JSONObject dataObj = new JSONObject();
        dataObj.put("cell", phoneNumber);
        dataObj.put("module", "LOGIN");
        dataObj.put("otpTime", System.currentTimeMillis());
        PublicKey publicKeyFromString = RsaUtil.getPublicKeyFromString(publicKey);
        String encrypt = RsaUtil.encrypt(JSONObject.toJSONString(dataObj), publicKeyFromString);
        JSONObject paramObj = new JSONObject();
        paramObj.put("data", encrypt);
        return JSONObject.toJSONString(paramObj);
    }
}
