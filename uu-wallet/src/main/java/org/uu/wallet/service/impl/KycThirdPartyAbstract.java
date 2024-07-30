package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.wallet.entity.KycBank;
import org.uu.wallet.service.IKycBankService;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.util.RequestUtil;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author lukas
 */
@Slf4j
public abstract class KycThirdPartyAbstract {
    @Resource
    IKycBankService kycBankService;
    @Resource
    RedisUtil redisUtil;

    protected String apiUrl;

    /**
     * 设置当前请求api地址
     * @param bankCode 银行code
     * @throws MalformedURLException ex
     */
    public void setHttpUrl(String bankCode) throws MalformedURLException {
        KycBank kycBank = kycBankService.getBankInfoByBankCode(bankCode);
        String apiUrl = kycBank.getApiUrl();
        URL url = new URL(apiUrl);
        this.apiUrl = url.getProtocol() + "://" +  url.getHost();
        if(ObjectUtils.isNotEmpty(url.getPort())
            && url.getPort() != -1
        ){
            this.apiUrl  +=  ":" + url.getPort();
        }
    }

    /**
     * 发送验证码
     * @param phoneNumber phoneNumber
     * @param bankCode bankCode
     * @return String
     * @throws MalformedURLException ex
     */
    public String sendOtpCode(String phoneNumber, String bankCode) throws MalformedURLException {
        setHttpUrl(bankCode);
        String url = apiUrl + "/sendOtp";
        JSONObject params = new JSONObject();
        params.put("phonenumber", phoneNumber);
        log.info("发送第三方接口kyc验证码： url：{}, bankCode:{}, phoneNumber:{}", url, bankCode, phoneNumber);
        String result =  RequestUtil.HttpRestClientToJson(url, JSONObject.toJSONString(params));
        log.info("发送第三方接口kyc验证码： url：{}, params:{}, result:{}", url, params, result);
        return result;
    }

    /**
     * 发送验证码
     * @param param param
     * @param bankCode bankCode
     * @return String
     * @throws MalformedURLException ex
     */
    public String sendOtpCode(String param, String bankCode, String path) throws MalformedURLException {
        setHttpUrl(bankCode);
        String url = apiUrl + path;
        log.info("发送银行接口kyc验证码： url：{}, bankCode:{}, param:{}", url, bankCode, param);
        JSONObject headerObj = new JSONObject();
        headerObj.put("X-Mclient", 0);
        Headers headers = JsonUtil.jsonToHeader(JSONObject.toJSONString(headerObj));
        Map<String, Object> result = RequestUtil.HttpRestClientToJsonWithHeader(url, param, headers);
        log.info("发送银行接口kyc验证码： url：{}, params:{}, result:{}", url, param, result);
        if(ObjectUtils.isNotEmpty(result)
            && result.containsKey("response")
        ){
            String responseStr = (String) result.get("response");
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            if (jsonObject.containsKey("success")
                    && jsonObject.containsKey("data")
                    && jsonObject.getBoolean("success")
            ) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data.containsKey("requestProcessed")
                        && data.containsKey("requestProcessTimeId")
                        && data.getBoolean("requestProcessed")
                ) {
                    return data.getString("requestProcessTimeId");
                }
            }
        }
        return null;
    }

    /**
     * 验证短信
     * @param params
     * @return
     * @throws MalformedURLException
     */
    public String sendVerifyCode(String bankCode, String params) throws MalformedURLException {
        setHttpUrl(bankCode);
        String url = apiUrl + "/verifyOtp";
        log.info("验证第三方接口kyc验证码： url：{}, bankCode:{}, 请求参数:{}", url, bankCode, params);
        String result = RequestUtil.HttpRestClientToJson(url, params);
        if(ObjectUtils.isEmpty(result)
            || !JsonUtil.isValidJSONObjectOrArray(result)
        ){
            return null;
        }
        JSONObject paramsObj = JSONObject.parseObject(params);
        JSONObject resultObj = JSONObject.parseObject(result);
        resultObj.put("phoneNumber", paramsObj.getString("phonenumber"));
        log.info("验证第三方接口kyc验证码： url：{}, params:{}, result:{}", url, params, result);
        return JSONObject.toJSONString(resultObj);
    }

    /**
     * 验证短信
     * @param params params
     * @return String
     * @throws MalformedURLException ex
     */
    public String sendVerifyCode(String bankCode, String params, String path) throws MalformedURLException {
        setHttpUrl(bankCode);
        String url = apiUrl + path;
        log.info("验证银行接口kyc验证码： url：{}, bankCode:{}, 请求参数:{}", url, bankCode, params);
        JSONObject headerObj = new JSONObject();
        headerObj.put("X-Mclient", 0);
        Headers headers = JsonUtil.jsonToHeader(JSONObject.toJSONString(headerObj));
        Map<String, Object> result = RequestUtil.HttpRestClientToJsonWithHeader(url, params, headers);
        if (ObjectUtils.isEmpty(result)
                || !result.containsKey("response")
                || !result.containsKey("header")
        ) {
            return null;
        }
        Headers resposeHeaders = (Headers) result.get("header");
        String hasId = resposeHeaders.get("Hashid");
        String token = resposeHeaders.get("Token");
        if(ObjectUtils.isEmpty(hasId)
            || ObjectUtils.isEmpty(token)
        ){
            return null;
        }
        String resultToken = hasId + "." + token;
        log.info("验证银行接口kyc验证码： url：{}, params:{}, result:{}, header:{}", url, params, result, resposeHeaders);
        JSONObject object = new JSONObject();
        object.put("Authorization", resultToken);
        object.put("X-MClient", 0);
        return JSONObject.toJSONString(object);
    }

}
