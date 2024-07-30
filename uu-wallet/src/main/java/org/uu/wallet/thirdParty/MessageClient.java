package org.uu.wallet.thirdParty;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RefreshScope
@Slf4j
public class MessageClient {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${third.party.message.appid:63313}")
    private String appid;

    @Value("${third.party.message.appkey:e224f6dc69215784d3e2dfe331ecd500}")
    private String appkey;

    @Value("${third.party.message.urlRoot:https://api-v4.mysubmail.com/}")
    private String urlRoot;

    /**
     *
     * @param telephone 目标手机号
     * @param content   发送内容
     * @return
     */
    public MessageStatus sendMessage(String telephone,String content){
        if(StringUtils.isEmpty(telephone) || StringUtils.isEmpty(content)){
            MessageStatus result =  new MessageStatus();
            result.setStatus(false);
            result.setMsg("telephone or content is invalid");
        }
        TreeMap<String, String> parameterMap = new TreeMap<>();
        parameterMap.put("appid",appid);
        parameterMap.put("to", telephone);
        parameterMap.put("timestamp", getTimestamp());
        parameterMap.put("sign_type", "md5");
        parameterMap.put("sign_version", "2");
        String signStr = appid + appkey + formatRequest(parameterMap) + appid + appkey;
        parameterMap.put("signature",encode("md5",signStr));
        parameterMap.put("content", content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result =
                restTemplate.postForEntity(urlRoot +"/internationalsms/send.json", requestEntity, JSONObject.class);
        log.info("单条短信发送.手机号:{},短息内容:{},返回信息:{}",telephone,content,result.getBody());
        return new MessageStatus(result.getBody());
    }


    /**
     *
     * @param telephoneList 手机号,最多10000个
     * @param content 短信内容
     * @return
     */
    public List<MessageStatus> sendMessage(List<String> telephoneList,String content){
        if(telephoneList.size()>10000 || telephoneList.size()<1 || StringUtils.isEmpty(content)){
            MessageStatus result =  new MessageStatus();
            result.setStatus(false);
            result.setMsg("telephone or content is invalid");
        }
        TreeMap<String, String> parameterMap = new TreeMap<>();
        parameterMap.put("appid",appid);
        String telephoneString = telephoneList.stream().collect(Collectors.joining(","));
        parameterMap.put("to",telephoneString );
        parameterMap.put("timestamp", getTimestamp());
        parameterMap.put("sign_type", "md5");
        parameterMap.put("sign_version", "2");
        String signStr = appid + appkey + formatRequest(parameterMap) + appid + appkey;
        parameterMap.put("signature",encode("md5",signStr));
        parameterMap.put("content", content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result =
                restTemplate.postForEntity(urlRoot +"/internationalsms/batchsend.json", requestEntity, JSONObject.class);
        log.info("批量短信发送.手机号:{},短息内容:{},返回信息:{}",telephoneString,content,result.getBody());
        JSONObject jsonObject = result.getBody();
        List<MessageStatus> messageStatusList = new ArrayList<>();
        if(jsonObject.getString("status")!=null && jsonObject.getString("status").equals("success")){
            messageStatusList = jsonObject.getJSONArray("responses").stream().map(j->new MessageStatus((LinkedHashMap) j)).collect(Collectors.toList());
        }else{
            messageStatusList.add(new MessageStatus(result.getBody()));
        }
        return messageStatusList;
    }


    /**
     *
     * 模板方式发送短信
     *
     * @param telephone 电话号码
     * @param templateId 模板id
     * @param jsonObject 模板参数 使用json格式
     * @return
     */
    public MessageStatus sendMessage(String telephone, String templateId, JSONObject jsonObject ){
        if(StringUtils.isEmpty(telephone) || StringUtils.isEmpty(templateId)){
            MessageStatus result =  new MessageStatus();
            result.setStatus(false);
            result.setMsg("telephone or templateId or jsonObject invalid");
        }

        if (jsonObject == null){
            jsonObject = new JSONObject();
        }

        TreeMap<String, String> parameterMap = new TreeMap<>();
        parameterMap.put("appid",appid);
        parameterMap.put("to",telephone );
        parameterMap.put("timestamp", getTimestamp());
        parameterMap.put("project", templateId);
        parameterMap.put("sign_type", "md5");
        parameterMap.put("sign_version", "2");
        String signStr = appid + appkey + formatRequest(parameterMap) + appid + appkey;
        parameterMap.put("signature",encode("md5",signStr));
        parameterMap.put("vars",jsonObject.toJSONString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result =
                restTemplate.postForEntity(urlRoot +"/internationalsms/xsend.json", requestEntity, JSONObject.class);
        log.info("短信模板发送.手机号:{},模板ID:{},请求参数:{},返回信息:{}",telephone,templateId,parameterMap,result.getBody());
        return new MessageStatus(result.getBody());
    }


    /**
     *
     * @param templateId  模板主键
     * @param messageMultiObject  模板信息
     * @return
     */
    public List<MessageStatus> sendMessage(String templateId,MessageMultiObject messageMultiObject  ){
        if(StringUtils.isEmpty(templateId) || ObjectUtils.isEmpty(messageMultiObject)||
                messageMultiObject.getJsonArray().isEmpty() ){
            MessageStatus result =  new MessageStatus();
            result.setStatus(false);
            result.setMsg("templateId or messageMultiObject  invalid");
        }
        TreeMap<String, String> parameterMap = new TreeMap<>();
        parameterMap.put("appid",appid);
        parameterMap.put("timestamp", getTimestamp());
        parameterMap.put("project", templateId);
        parameterMap.put("sign_type", "md5");
        parameterMap.put("sign_version", "2");
        String signStr = appid + appkey + formatRequest(parameterMap) + appid + appkey;
        parameterMap.put("signature",encode("md5",signStr));
        parameterMap.put("multi", messageMultiObject.toJSONString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONArray> result =
                restTemplate.postForEntity(urlRoot +"/internationalsms/multixsend.json", requestEntity, JSONArray.class);
        log.info("短信批量模板发送.模板ID:{},模板信息:{},返回信息:{}",templateId,messageMultiObject.toJSONString(),result.getBody());
        JSONArray jsonArray = result.getBody();
        return jsonArray.stream().map(j->new MessageStatus((LinkedHashMap) j)).collect(Collectors.toList());
    }


    private String getTimestamp() {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject result = restTemplate.getForObject(urlRoot+"service/timestamp",JSONObject.class);
        return result.getString("timestamp");
    }



    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encode(String algorithm, String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(str.getBytes("UTF-8"));
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static String formatRequest(Map<String, String> data) {
        Set<String> keySet = data.keySet();
        Iterator<String> it = keySet.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            String key = it.next();
            Object value = data.get(key);
            if (value instanceof String) {
                sb.append(key + "=" + value + "&");
            }
        }
        if (sb.length() != 0) {
            System.out.println("sb.substring(0, sb.length() - 1) = " + sb.substring(0, sb.length() - 1));
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

}
