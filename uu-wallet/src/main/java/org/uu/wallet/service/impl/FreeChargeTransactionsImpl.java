package org.uu.wallet.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.*;
import java.util.stream.Collectors;

@Service("freeCharge")
@RequiredArgsConstructor
@Slf4j
public class FreeChargeTransactionsImpl implements IAppBankTransaction {

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
        KycBank kycBank = kycBankService.getBankInfoByBankCode("freeCharge");

        //将token转为赋值到请求头
        Headers headers = JsonUtil.jsonToHeader(kycToken);

        if (headers == null) {
            kycBankResponseVo.setMsg("Invalid kycToken");
            return kycBankResponseVo;
        }

        JSONObject reqJson = new JSONObject();

        //收入
//        reqJson.put("direction", "CREDIT");

        //支出
//        reqJson.put("direction", "DEBIT");

        //请求银行, 根据银行返回结果判断是否连接成功
        String resStr = RequestUtil.getForAppJson(kycBank.getApiUrl(), reqJson, headers);

        if (StringUtil.isEmpty(resStr) || !JsonUtil.isValidJSONObjectOrArray(resStr)) {
            log.error("freeCharge 连接KYC银行失败, 请求银行接口返回数据为null, resStr: {}", resStr);

            kycBankResponseVo.setMsg(resStr);
            return kycBankResponseVo;
        }

        JSONObject resJson = JSONObject.parseObject(resStr);
        String error = resJson.getString("error");

        log.info("freeCharge 连接KYC银行, error: {}", error);
        if(error != null){
            JSONObject errorObj = JSONObject.parseObject(error);
            if(errorObj.containsKey("errorCode") && Objects.equals(errorObj.getString("errorCode"), "ER-5100")){
                kycBankResponseVo.setStatus(true);
                return kycBankResponseVo;
            }
        }
        if (error == null) {
            kycBankResponseVo.setStatus(true);
            return kycBankResponseVo;
        }

        kycBankResponseVo.setMsg(resStr);
        return kycBankResponseVo;
    }

    @Override
    public List<BankKycTransactionVo> getKYCBankTransactions(String kycToken) {

        List<BankKycTransactionVo> resultList = new ArrayList<>();

        //获取银行信息
        KycBank kycBank = kycBankService.getBankInfoByBankCode("freeCharge");

        //将token转为赋值到请求头
        Headers headers = JsonUtil.jsonToHeader(kycToken);

        if (headers == null) {
            return resultList;
        }

        JSONObject reqJson = new JSONObject();

        //收入
//        reqJson.put("direction", "CREDIT");

        //支出
//        reqJson.put("direction", "DEBIT");

        //请求银行, 根据银行返回结果判断是否连接成功
        String resStr = RequestUtil.getForAppJson(kycBank.getApiUrl(), reqJson, headers);

        if (StringUtil.isEmpty(resStr) || !JsonUtil.isValidJSONObjectOrArray(resStr)) {
            log.error("freeCharge 连接KYC银行失败, 请求银行接口返回数据为null, resStr: {}", resStr);
            return resultList;
        }

        JSONObject resJson = JSONObject.parseObject(resStr);
        Object error = resJson.get("error");

        log.info("freeCharge 连接KYC银行, error: {}", error);

        if (error == null) {
            filter(resultList, resJson);
            return resultList;
        } else {
            return resultList;

        }
    }


    private void filter(List<BankKycTransactionVo> resultList, JSONObject resJson) {
        List<Map<String, Object>> dataList = resJson.getObject("data", List.class);
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        List<BankKycTransactionVo> filteredData = dataList.stream()
                .filter(map -> map != null && "SUCCESS".equals(map.get("txnStatus")))
                .map(map -> createBankKycTransactionVo(map))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(filteredData.size() > 50){
            filteredData = filteredData.subList(0, 50);
        }
        resultList.addAll(filteredData);
    }


    private BankKycTransactionVo createBankKycTransactionVo(Map<String, Object> dataMap) {
        if (dataMap.isEmpty()) {
            return null;
        }

        BankKycTransactionVo bankKycTransactionVo = new BankKycTransactionVo();


        Object amountObj = dataMap.get("txnAmount");
        BigDecimal amount = null;
        if (amountObj instanceof BigDecimal) {
            amount = (BigDecimal) amountObj;
        } else if (amountObj instanceof String) {
            try {
                amount = new BigDecimal((String) amountObj);
            } catch (NumberFormatException e) {
                log.error("获取KYC交易记录失败 Invalid format for txnAmount", e);
                // 处理错误或设置amount为null或默认值
            }
        }

        // 订单金额
        bankKycTransactionVo.setAmount(amount);

        // 订单状态 1: 成功
        bankKycTransactionVo.setOrderStatus((String) dataMap.get("txnStatus"));
        if ("SUCCESS".equals(bankKycTransactionVo.getOrderStatus())){
            bankKycTransactionVo.setOrderStatus("1");
        }

        // 订单类型 收入: 1  支出: 2
        bankKycTransactionVo.setMode((String) dataMap.get("paymentDirection"));
        if ("CREDIT".equals(bankKycTransactionVo.getMode())){
            bankKycTransactionVo.setMode("1");
        }

        if ("DEBIT".equals(bankKycTransactionVo.getMode())){
            bankKycTransactionVo.setMode("2");
        }

        // 解析 txnHistory 中的 upiinfo
        Map<String, Object> txnHistory = (Map<String, Object>) dataMap.get("txnHistory");
        if (txnHistory != null && txnHistory.containsKey("upiinfo")) {
            Map<String, Object> upiInfo = (Map<String, Object>) txnHistory.get("upiinfo");
            if (upiInfo != null) {

                // UTR
                bankKycTransactionVo.setUTR((String) upiInfo.get("retrievalReferenceNum"));

                //付款人UPI
                bankKycTransactionVo.setPayerUPI((String) upiInfo.get("sourceVpa"));

                // 收款人UPI
                String vpa = (String) upiInfo.get("destVpa");
                if(ObjectUtils.isNotEmpty(vpa) && vpa.contains("@") && vpa.contains("ifsc")){
                    String[] split = vpa.split("@");
                    String cardNumber = split[0];
                    if(cardNumber.length() > 4){
                        vpa = cardNumber.substring(cardNumber.length() - 4);
                    }
                }
                bankKycTransactionVo.setRecipientUPI(vpa);
            }
        }

        // 处理时间戳，转换为指定格式
        Long timestamp = (Long) dataMap.get("timestamp");

        // 检查时间戳是否为13位的毫秒级，如果是，则转换为10位秒级
        if (timestamp != null && String.valueOf(timestamp).length() == 13) {
            timestamp = timestamp / 1000;  // 从毫秒转换为秒
        }

        if (timestamp != null) {
            bankKycTransactionVo.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()));
        }
        return bankKycTransactionVo;
    }
}
