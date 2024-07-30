package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.wallet.vo.BankKycTransactionVo;
import org.uu.wallet.vo.KycBankResponseVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lukas
 */
@Service("airtel")
@RequiredArgsConstructor
@Slf4j
public class AirtelTransactionsImpl extends AbstractBankTransactions implements IAppBankTransaction {

    /**
     * 连接KYC银行
     *
     * @param kycToken kyc
     * @return {@link KycBankResponseVo}
     */
    @Override
    public KycBankResponseVo linkKycPartner(String kycToken) {
        JSONObject jsonObject = JSONObject.parseObject(kycToken);
        JSONObject params = new JSONObject();
        params.put("phonenumber", jsonObject.getString("phoneNumber"));
        params.put("verified_data", jsonObject.getString("verify_response"));
        return linkKycPartnerAccomplish(JSONObject.toJSONString(params), "airtel");
    }

    @Override
    public List<BankKycTransactionVo> getKYCBankTransactions(String kycToken) {
        JSONObject jsonObject = JSONObject.parseObject(kycToken);
        JSONObject params = new JSONObject();
        params.put("phonenumber", jsonObject.getString("phoneNumber"));
        params.put("verified_data", jsonObject.getString("verify_response"));
        return getKycBankTransactionAccomplish(JSONObject.toJSONString(params), "airtel");
    }

    @Override
    public boolean checkResult(String resultStr){
        JSONObject resJson = JSONObject.parseObject(resultStr);
        String metaStr = resJson.getString("meta");
        JSONObject jsonObject = JSONObject.parseObject(metaStr);
        if(jsonObject.containsKey("status")){
            Integer status = jsonObject.getInteger("status");
            return status == 0;
        }
        return false;
    }

    /**
     * 过滤数据
     * @param resultList resultList
     * @param resJson resJson
     */
    @Override
    public void filter(List<BankKycTransactionVo> resultList, JSONObject resJson){
        if(!resJson.containsKey("data")
                || resJson.get("data") == null
                || !(resJson.get("data") instanceof Map)){
            return;
        }
        Map data = resJson.getObject("data", Map.class);
        if(data.isEmpty()
                || !data.containsKey("txnDetails")
                || !(data.get("txnDetails") instanceof List)
        ){
            return;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("txnDetails");
        for (Map<String, Object> stringObjectMap : list) {
            if (stringObjectMap.isEmpty()
                    || !stringObjectMap.containsKey("txnDate")
                    || !stringObjectMap.containsKey("txnStatus")
//                    || !"CREDIT".equals(String.valueOf(stringObjectMap.get("txnType")))
                    || !"SUCCESS".equals(String.valueOf(stringObjectMap.get("txnStatus")))
            ) {
                continue;
            }
            BankKycTransactionVo vo = new BankKycTransactionVo();
            String status = stringObjectMap.containsKey("txnStatus") ? (String) stringObjectMap.get("txnStatus") : null;
            String date = stringObjectMap.containsKey("txnDate") ? (String) stringObjectMap.get("txnDate") : null;
            String mode = stringObjectMap.containsKey("txnType") ? (String) stringObjectMap.get("txnType") : null;
            String upiRefId = stringObjectMap.containsKey("upiRefId") ? (String) stringObjectMap.get("upiRefId") : null;
            String payerVpa = stringObjectMap.containsKey("payerVpa") ? (String) stringObjectMap.get("payerVpa") : null;
            String payeeVpa = stringObjectMap.containsKey("payeeVpa") ? (String) stringObjectMap.get("payeeVpa") : null;
            String payeeAccountNumber = stringObjectMap.containsKey("payeeAccountNumber") ? (String) stringObjectMap.get("payeeAccountNumber") : null;
            BigDecimal amount = stringObjectMap.containsKey("amount") ? (BigDecimal) stringObjectMap.get("amount") : null;
            vo.setUTR(upiRefId);
            vo.setAmount(amount);
            vo.setOrderStatus("2");
            if(Objects.equals(status, "SUCCESS")){
                vo.setOrderStatus("1");
            }
            vo.setMode("2");
            if(Objects.equals(mode, "CREDIT")){
                vo.setMode("1");
            }
            vo.setPayerUPI(payerVpa);
            vo.setRecipientUPI(payeeVpa);
            if(ObjectUtils.isNotEmpty(payeeAccountNumber)){
                if(payeeAccountNumber.length() > 4){
                    payeeAccountNumber = payeeAccountNumber.substring(payeeAccountNumber.length() - 4);
                    vo.setRecipientUPI(payeeAccountNumber);
                }
            }
            if(payeeVpa != null && payeeVpa.contains("@") && payeeVpa.contains("ifsc")){
                    String[] split = payeeVpa.split("@");
                    String cardNumber = split[0];
                    if(cardNumber.length() > 4){
                        cardNumber = cardNumber.substring(cardNumber.length() - 4);
                        vo.setRecipientUPI(cardNumber);
                    }
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            if(date != null){
                LocalDateTime dateParse = LocalDateTime.parse(date, dateTimeFormatter);
                vo.setCreateTime(dateParse);
            }
            vo.setDetail(JSONObject.toJSONString(stringObjectMap));
            resultList.add(vo);
        }
    }
}
