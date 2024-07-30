package org.uu.wallet.thirdParty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.pay.dto.SmsBalanceWarnDTO;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.mapper.TradeConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RefreshScope
@Data
@Slf4j
public class TelephoneClient {


    private RestTemplate restTemplate = new RestTemplate();

    @Value("${third.party.telephone.appid:JXKZHLBb}")
    private String appId;

    @Value("${third.party.telephone.urlRoot:https://api.itniotech.com/}")
    private String urlRoot;

    @Value("${third.party.telephone.apiKey:nI2RW0IXOqx9bopvYXAIu5NxJA8wVkwP}")
    private String apiKey;

    @Value("${third.party.telephone.apiPassword:AwJ638bZdK3MfNPWvOyMBCxvZUN4G6ki}")
    private String apiPassword;

    @Value("${third.party.telephone.defaultVoiceId:120231121f48b5ec1b9fa4d88bb4ea47a75da6db3.mp3}")
    private String defaultVoiceId;

    private final TradeConfigMapper tradeConfigMapper;


    /**
     * @param telephoneList 发送的手机号集合
     * @return
     */
    public List<TelephoneStatus> sendVoice(List<String> telephoneList) {

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigMapper.selectById(1);

        //查看是否开启语音通知
        if (tradeConfig == null || tradeConfig.getVoicePaymentReminderEnabled() != 1) {

            log.info("未开启语音通知, telephoneList: {}", telephoneList);

            TelephoneStatus telephoneStatus = new TelephoneStatus();
            telephoneStatus.setStatus(true);

            ArrayList<TelephoneStatus> resList = new ArrayList<>();
            resList.add(telephoneStatus);

            return resList;
        }

        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        String nowTime = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        ;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest((apiKey + apiPassword + nowTime).getBytes());

        StringBuilder sign = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sign.append('0');
            }
            sign.append(hex);
        }
        headers.set("Timestamp", nowTime);
        headers.set("Sign", sign.toString());
        headers.set("Api-Key", apiKey);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("callee", telephoneList.stream().collect(Collectors.joining(",")));
        parameterMap.put("fileId", defaultVoiceId);
        parameterMap.put("appId", appId);
        parameterMap.put("loopCount", 2);
        parameterMap.put("maxDuration", 39);

        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result = restTemplate.postForEntity(urlRoot + "/voice/sendGroup", requestEntity, JSONObject.class);
        JSONArray jsonArray = result.getBody().getJSONArray("data");
        if (jsonArray != null) {
            return jsonArray.stream().map(j -> {
                LinkedHashMap lh = (LinkedHashMap) j;
                if ("0".equals(lh.get("terminationCode"))) {
                    return new TelephoneStatus(true, lh.get("terminationReason").toString(), lh.get("voiceId").toString(),
                            lh.get("callee").toString());
                } else {
                    return new TelephoneStatus(false, lh.get("terminationReason").toString(), lh.get("voiceId").toString()
                            , lh.get("callee").toString());
                }
            }).collect(Collectors.toList());
        }

        return Collections.singletonList(new TelephoneStatus(
                result.getBody().getString("status") != null ? "0".equals(result.getBody().getString("status"))
                        : false,
                result.getBody().getString("reason"), null));
    }


    /**
     * @param voiceId 手机发送回执
     * @return
     */
    public TelephoneStatus getStatusByVoiceId(String voiceId) {
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        String nowTime = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest((apiKey + apiPassword + nowTime).getBytes());

        StringBuilder sign = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sign.append('0');
            }
            sign.append(hex);
        }
        headers.set("Timestamp", nowTime);
        headers.set("Sign", sign.toString());
        headers.set("Api-Key", apiKey);


        HttpEntity<Map> requestEntity = new HttpEntity(null, headers);
        ResponseEntity<JSONObject> result = restTemplate.exchange(urlRoot + "/voice/recordGroup/" + voiceId,
                HttpMethod.GET, requestEntity, JSONObject.class);
        JSONObject jsonbject = result.getBody().getJSONObject("data");
        if (jsonbject != null) {
            if ("0".equals(jsonbject.get("terminationCode"))) {
                return new TelephoneStatus(true, jsonbject.get("terminationReason").toString(), jsonbject.get("voiceId").toString()
                        , jsonbject.get("callee").toString());
            } else {
                return new TelephoneStatus(false, jsonbject.get("terminationReason").toString(), jsonbject.get("voiceId").toString()
                        , jsonbject.get("callee").toString());
            }
        }

        return new TelephoneStatus(
                result.getBody().getString("status") != null ? "0".equals(result.getBody().getString("status"))
                        : false,
                result.getBody().getString("reason"));
    }


    /**
     * 获取短信账户余额
     *
     * @return
     */
    public SmsBalance getBalance() {
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        String nowTime = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        ;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest((apiKey + apiPassword + nowTime).getBytes());

        StringBuilder sign = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sign.append('0');
            }
            sign.append(hex);
        }
        headers.set("Timestamp", nowTime);
        headers.set("Sign", sign.toString());
        headers.set("Api-Key", apiKey);

        HttpEntity<Map> requestEntity = new HttpEntity(null, headers);
        ResponseEntity<SmsBalance> result = restTemplate.exchange(urlRoot + "/sms/getBalance", HttpMethod.GET, requestEntity, SmsBalance.class);
        //log.info("调用获取短信余额接口返回:{}", result);
        return result.getBody();
    }


    /**
     * 校验短信账户余额：异常返回值空
     *
     * @return
     */
    public SmsBalanceWarnDTO checkBalance() {
        try {
            SmsBalance smsBalance = this.getBalance();
            if (smsBalance == null || !"0".equals(smsBalance.getStatus())) {
                log.warn("检查短信账户余额, 调用短信接口失败, 无法校验, 三方返回:{}", smsBalance);
                return null;
            }
            TradeConfig tradeConfig = tradeConfigMapper.selectById(1);
            if (tradeConfig.getMessageBalanceThreshold() == null) {
                log.warn("检查短信账户余额, 告警阈值未配置, 无法校验");
                return null;
            }
            BigDecimal currentBalance = new BigDecimal(smsBalance.getBalance());
            log.info("检查短信账户余额, 当前账户余额:{}, 告警阈值:{}", currentBalance, tradeConfig.getMessageBalanceThreshold());
            Boolean isWarn = currentBalance.compareTo(tradeConfig.getMessageBalanceThreshold()) < 0 ? Boolean.TRUE : Boolean.FALSE;
            return SmsBalanceWarnDTO.builder().currentBalance(currentBalance).threshold(tradeConfig.getMessageBalanceThreshold()).isWarn(isWarn).build();
        } catch (Exception e) {
            log.error("检查短信账户余额失败:", e);
        }
        return null;
    }

}
