package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.wallet.vo.BankKycTransactionVo;
import org.uu.wallet.vo.KycBankResponseVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lukas
 */
@Slf4j
@Service("phonepe")
public class PhonepeTransactionsImpl extends AbstractBankTransactions implements IAppBankTransaction {

    @Override
    public KycBankResponseVo linkKycPartner(String kycToken) {
        return linkKycPartnerAccomplish(getRequestBody(kycToken), "phonepe");
    }


    @Override
    public List<BankKycTransactionVo> getKYCBankTransactions(String kycToken) {
        return getKycBankTransactionAccomplish(getRequestBody(kycToken), "phonepe");
    }

    @Override
    public boolean checkResult(String resultStr) {
        JSONObject resultJson = JSON.parseObject(resultStr);
        return resultJson.containsKey("code")
                && resultJson.getInteger("code") == 200
                && resultJson.containsKey("success")
                && resultJson.getBoolean("success");
    }

    @Override
    public void filter(List<BankKycTransactionVo> resultList, JSONObject resJson) {
        if(!resJson.containsKey("response")
                || resJson.get("response") == null
                || !(resJson.get("response") instanceof Map)){
            return;
        }
        Map response = resJson.getObject("response", Map.class);
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("changes");
        for (Map<String, Object> stringObjectMap : list) {
            BankKycTransactionVo vo = new BankKycTransactionVo();
            if(!stringObjectMap.containsKey("state")
                    || !Objects.equals(stringObjectMap.get("state"), "COMPLETED")
            ){
                continue;
            }
            vo.setMode("1");
            if(stringObjectMap.containsKey("entityId")){
                Map<String, String> entityId = (Map<String, String>) stringObjectMap.get("entityId");
                if(!entityId.containsKey("type")
                        || !Objects.equals(entityId.get("type"), "RECEIVED_PAYMENT")
                ){
                    vo.setMode("2");
                }
            }
            vo.setOrderStatus("1");
            if(stringObjectMap.containsKey("data")
                    && stringObjectMap.get("data") instanceof Map
            ){
                Map<String, Object> data = (Map<String, Object>) stringObjectMap.get("data");
                //
                if(Objects.equals(vo.getMode(), "1")){
                    List<Map<String, String>> receivedIn = (List<Map<String, String>>) data.get("receivedIn");
                    Map<String, String> receivedInMap = receivedIn.get(0);
                    String receivedInVpa = receivedInMap.getOrDefault("vpa", "");
                    String utr = receivedInMap.getOrDefault("utr", "");
                    vo.setRecipientUPI(receivedInVpa);
                    vo.setUTR(utr);
                    if(data.containsKey("from")
                            && data.get("from") instanceof Map
                    ){
                        Map<String, String> fromData = (Map<String, String>) data.get("from");
                        String fromVpa = fromData.getOrDefault("vpa", "");
                        vo.setPayerUPI(fromVpa);
                    }
                }else{
                    List<Map<String, String>> to = (List<Map<String, String>>) data.get("to");
                    List<Map<String, String>> paidFrom = (List<Map<String, String>>) data.get("paidFrom");
                    if(ObjectUtils.isEmpty(to)
                            || ObjectUtils.isEmpty(paidFrom)){
                        continue;
                    }
                    Map<String, String> toMap = to.get(0);
                    String vpa = toMap.getOrDefault("fullVpa", "");
                    String name = toMap.getOrDefault("name", "");
                    if(name.startsWith("Bank Account ")){
                        name = name.substring(name.length() - 4);
                        vo.setRecipientUPI(name);
                    }
                    if(vpa.contains("@")){
                        String[] split = vpa.split("@");
                        if(split[0].length() > 4){
                            vo.setRecipientUPI(split[0].substring(split[0].length() - 4));
                        }
                    }

                    Map<String, String> paidFromMap = paidFrom.get(0);
                    vo.setPayerUPI(paidFromMap.getOrDefault("vpa", ""));
                    vo.setUTR(paidFromMap.getOrDefault("utr", ""));
                }
            }
            if(stringObjectMap.containsKey("tags")){
                Map<String, Object> tags = (Map<String, Object>) stringObjectMap.get("tags");
                BigDecimal amount = tags.containsKey("account.amount") ? new BigDecimal(String.valueOf(tags.get("account.amount"))) : BigDecimal.ZERO;
                vo.setAmount(amount);
            }
            if(stringObjectMap.containsKey("created")){
                long created = stringObjectMap.containsKey("created") ? (long) stringObjectMap.get("created") : 0;
                Instant instant = Instant.ofEpochMilli(created);
                ZoneId zone = ZoneId.systemDefault();
                LocalDateTime createTime = LocalDateTime.ofInstant(instant, zone);
                vo.setCreateTime(createTime);
            }
            vo.setDetail(JSONObject.toJSONString(stringObjectMap));
            resultList.add(vo);
        }
    }

    private String getRequestBody(String kycToken){
        JSONObject req = JSONObject.parseObject(kycToken);
        JSONObject body = new JSONObject();
        body.put("get_authtoken_resp", req.get("get_authtoken_resp"));
        body.put("device_id", req.get("device_id"));
        return JSONObject.toJSONString(body);
    }
}
