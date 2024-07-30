package org.uu.wallet.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.uu.wallet.entity.KycBank;
import org.uu.wallet.service.IKycBankService;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.vo.BankKycTransactionVo;
import org.uu.wallet.vo.KycBankResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("mobikwik")
@RequiredArgsConstructor
@Slf4j
public class MobikwikTransactionsImpl implements IAppBankTransaction {

    @Autowired
    private IKycBankService kycBankService;

    /**
     * 连接KYC银行
     *
     * @param kycToken
     * @return {@link KycBankResponseVo}
     */
    @Override
    public KycBankResponseVo linkKycPartner(String kycToken) {

        KycBankResponseVo kycBankResponseVo = new KycBankResponseVo();

        //获取银行信息
        KycBank kycBank = kycBankService.getBankInfoByBankCode("mobikwik");

        //将token转为赋值到请求头
        Headers headers = JsonUtil.jsonToHeader(kycToken);

        if (headers == null) {
            kycBankResponseVo.setMsg("Invalid kycToken");
            return kycBankResponseVo;
        }

        //请求银行, 根据银行返回结果判断是否连接成功
        String resStr = RequestUtil.get(kycBank.getApiUrl(), null, headers);

        if (StringUtil.isEmpty(resStr) || !JsonUtil.isValidJSONObjectOrArray(resStr)) {
            log.error("连接KYC银行失败, 请求银行接口返回数据为null, resStr: {}", resStr);

            kycBankResponseVo.setMsg(resStr);
            return kycBankResponseVo;
        }

        JSONObject resJson = JSONObject.parseObject(resStr);

        Boolean success = resJson.getBoolean("success");

        if (success != null && success) {
            kycBankResponseVo.setStatus(true);
            return kycBankResponseVo;
        }

        kycBankResponseVo.setMsg(resStr);
        return kycBankResponseVo;
    }

    @Override
    public List<BankKycTransactionVo> getKYCBankTransactions(String kycToken) {

        //获取银行信息
        KycBank kycBank = kycBankService.getBankInfoByBankCode("mobikwik");

        List<BankKycTransactionVo> resultList = new ArrayList<>();

        //将token转为赋值到请求头
        Headers headers = JsonUtil.jsonToHeader(kycToken);

        if (headers == null) {
            return resultList;
        }

        //请求银行, 根据银行返回结果判断是否连接成功
        String resStr = RequestUtil.get(kycBank.getApiUrl(), null, headers);

        if (StringUtil.isEmpty(resStr) || !JsonUtil.isValidJSONObjectOrArray(resStr)) {
            return resultList;
        }

        JSONObject resJson = JSONObject.parseObject(resStr);

        Boolean success = resJson.getBoolean("success");
        if (success != null && success) {
            filter(resultList, resJson);
            return resultList;
        }
        return resultList;
    }

    private void filter(List<BankKycTransactionVo> resultList, JSONObject resJson){
        if(!resJson.containsKey("data")
                || resJson.get("data") == null
                || !(resJson.get("data") instanceof Map)){
            return;
        }
        Map data = resJson.getObject("data", Map.class);
        if(data.isEmpty()
                || !data.containsKey("historyData")
                || !(data.get("historyData") instanceof List)
        ){
            return;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("historyData");
        for (Map<String, Object> stringObjectMap : list) {
            if (stringObjectMap.isEmpty()
                    || !stringObjectMap.containsKey("mode")
                    || !stringObjectMap.containsKey("status")
                    || !String.valueOf(stringObjectMap.get("status")).equals("success")
            ) {
                continue;
            }
            BankKycTransactionVo vo = new BankKycTransactionVo();
            String status = "0";
            if(stringObjectMap.containsKey("status") && stringObjectMap.get("status").equals("success")){
                status = "1";
            }
            Long timestamp = stringObjectMap.containsKey("date") ? (Long) stringObjectMap.get("date") : null;
            String mode = "2";
            if(stringObjectMap.containsKey("mode") && stringObjectMap.get("mode").equals("credit")){
                mode = "1";
            }
            String rrn = stringObjectMap.containsKey("rrn") ? (String) stringObjectMap.get("rrn") : null;
            String acquirerVPA = stringObjectMap.containsKey("acquirerVPA") ? (String) stringObjectMap.get("acquirerVPA") : null;
            BigDecimal amount = stringObjectMap.containsKey("amount") ? (BigDecimal) stringObjectMap.get("amount") : null;
            vo.setUTR(rrn);
            vo.setAmount(amount);
            vo.setOrderStatus(status);
            vo.setMode(mode);
            if(acquirerVPA.startsWith("XXXXXX")){
                acquirerVPA = acquirerVPA.substring(acquirerVPA.length()- 4, acquirerVPA.length());
            }
            vo.setRecipientUPI(acquirerVPA);
            vo.setPayerUPI(null);

            // 检查时间戳是否为13位的毫秒级，如果是，则转换为10位秒级
            if (timestamp != null && String.valueOf(timestamp).length() == 13) {
                timestamp = timestamp / 1000;  // 从毫秒转换为秒
            }

            if (timestamp != null) {
                vo.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()));
            }
            vo.setDetail(JSONObject.toJSONString(stringObjectMap));
            resultList.add(vo);
        }
    }
}
