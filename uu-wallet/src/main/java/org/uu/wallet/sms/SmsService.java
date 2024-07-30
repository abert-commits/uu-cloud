package org.uu.wallet.sms;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.RequestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
@RefreshScope
public class SmsService {

    //不卡短信
    @Value("${sms.baseUrl}")
    private String baseUrl;

    @Value("${sms.appId}")
    private String appId;

    @Value("${sms.apiKey}")
    private String apiKey;

    @Value("${sms.apiSecret}")
    private String apiSecret;


    //颂量短信
    @Value("${smsSl.baseUrl}")
    private String slBaseUrl;

    @Value("${smsSl.appId}")
    private String slAppId;

    @Value("${smsSl.apiKey}")
    private String slApiKey;

    @Value("${smsSl.apiSecret}")
    private String slApiSecret;


    // SMS短信
    @Value("${smsSMS.baseUrl:http://122.51.2.60:9511}")
    private String smsBaseUrl;

    @Value("${smsSMS.appId:356466}")
    private String smsAppId;

    @Value("${smsSMS.apiKey:29c03a33}")
    private String smsApiKey;

    /**
     * 不卡发送短信验证码
     *
     * @param numbers 手机号 如有多个已英文逗号分割
     * @param code 验证码
     * @return {@link Boolean}
     */
    public Boolean sendBkSms(String numbers, String code) {

        String content = "[uu-Wallet] Your verification code is " + code + ". please do not share this code with anyone.";

        final String senderId = "";

        final String url = baseUrl.concat("/sendSms");

        HttpRequest request = HttpRequest.post(url);

        final String datetime = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        final String sign = SecureUtil.md5(apiKey.concat(apiSecret).concat(datetime));

        request.header(Header.CONNECTION, "Keep-Alive")
                .header(Header.CONTENT_TYPE, "application/json;charset=UTF-8")
                .header("Sign", sign)
                .header("Timestamp", datetime)
                .header("Api-Key", apiKey);


        final String params = JSONUtil.createObj()
                .set("appId", appId)
                .set("numbers", numbers)
                .set("content", content)
                .set("senderId", senderId)
                .toString();

        HttpResponse response = request.body(params).execute();
        if (response.isOk()) {

            JSONObject resJson = JSON.parseObject(response.body());

            if (resJson.get("status").toString().equals("0")){
                log.info("不卡发送短信验证码成功: 手机号: {}, 验证码: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, code, url, params, resJson);
                return Boolean.TRUE;
            }
        }

        log.error("不卡发送短信验证码失败: 手机号: {}, 验证码: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, code, url, params, response.body());
        return Boolean.FALSE;
    }


    /**
     * 颂量发送短信验证码
     *
     * @param numbers 手机号 如有多个已英文逗号分割
     * @param code    验证码
     * @return {@link Boolean}
     */
    public Boolean sendSlSms(String numbers, String code) {

        String content = "[uu-Wallet] Your verification code is " + code + ". please do not share this code with anyone.";

        final String senderId = "";

        final String url = slBaseUrl.concat("/sms/sendSms");

        HttpRequest request = HttpRequest.post(url);

        final String datetime = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        final String sign = SecureUtil.md5(slApiKey.concat(slApiSecret).concat(datetime));

        request.header(Header.CONNECTION, "Keep-Alive")
                .header(Header.CONTENT_TYPE, "application/json;charset=UTF-8")
                .header("Sign", sign)
                .header("Timestamp", datetime)
                .header("Api-Key", slApiKey);


        final String params = JSONUtil.createObj()
                .set("appId", slAppId)
                .set("numbers", numbers)
                .set("content", content)
                .set("senderId", senderId)
                .toString();

        HttpResponse response = request.body(params).execute();
        if (response.isOk()) {

            JSONObject resJson = JSON.parseObject(response.body());

            if (resJson.get("status").toString().equals("0")){
                log.info("颂量发送短信验证码成功: 手机号: {}, 验证码: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, code, url, params, resJson);
                return Boolean.TRUE;
            }
        }

        log.error("颂量发送短信验证码失败: 手机号: {}, 验证码: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, code, url, params, response.body());
        return Boolean.FALSE;
    }


    /**
     * SMS发送交易通知
     *
     * @param numbers 手机号
     * @return {@link Boolean}
     */
    public Boolean sendTransactionNotification(String numbers) {

        String content = "[AR-Wallet] Your UPI withdrawal has been transferred successfully. If payment is received, please enter the order details and click \"Confirm Receipt\"";

        java.util.Map<String, String> paras = new java.util.HashMap<>();
        paras.put("sp_id", smsAppId);
        paras.put("mobile", numbers);
        paras.put("content", content);
        //根据参数Key排序（顺序）
        java.util.TreeMap<String, String> sortParas = new java.util.TreeMap<>();
        sortParas.putAll(paras);

        String sortedQueryString = specialUrlEncode(http_build_query(sortParas));

        //将上面得到的字符串按照如下顺序拼接成新的字符串
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("POST").append("&");
        stringToSign.append(toUtf8("/")).append("&");
        stringToSign.append(sortedQueryString);

        //签名采用HmacSHA1算法 + Base64。参考代码如下：
        String sign = getSignature(stringToSign.toString(), smsApiKey);

        paras.put("signature", sign);

        String url = smsBaseUrl + "/api/send-sms-single";

        String res = RequestUtil.sendPostRequest(url, paras);

        if (StringUtil.isEmpty(res) || !JsonUtil.isValidJSONObjectOrArray(res)) {
            log.error("SMS发送短信交易通知失败: 手机号: {}, 短信内容: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, content, url, paras, res);
            return false;
        }

        JSONObject resJson = JSONObject.parseObject(res);

        if ("0".equals(String.valueOf(resJson.get("code")))) {
            log.info("SMS发送短信交易通知成功: 手机号: {}, 短信内容: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, content, url, paras, resJson);
            return Boolean.TRUE;
        }

        log.error("SMS发送短信交易通知失败: 手机号: {}, 短信内容: {}, 请求地址: {}, 请求参数: {}, response: {}", numbers, content, url, paras, res);
        return Boolean.FALSE;
    }

    public static String specialUrlEncode(String value) {
        return value.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    public static String toUtf8(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Java实现PHP中的http_build_query()效果
     *
     * @param array key=value形式的二位数组
     * @return
     */
    public static String http_build_query(Map<String, String> array) {
        String reString = null;
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            reString += key + "=" + value + "&";
        }
        reString = reString.substring(0, reString.length() - 1);
        //将得到的字符串进行处理得到目标格式的字符串
        try {
            reString = java.net.URLEncoder.encode(reString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        reString = reString.replace("%3D", "=").replace("%26", "&");

        // 去除第一个多余的null符号
        return reString.substring(4);

    }

    public static String getSignature(String data, String key) {

        try {
            byte[] keyBytes = key.getBytes();

            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            Mac mac = Mac.getInstance("HmacSHA1");

            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

            String hexBytes = byte2hex(rawHmac);

            return Base64.getEncoder().encodeToString((hexBytes.getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    private static String byte2hex(final byte[] b) {
        String hs = "";

        String stmp = "";

        for (int n = 0; n < b.length; n++) {

            stmp = (java.lang.Integer.toHexString(b[n] & 0xFF));

            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }
}
