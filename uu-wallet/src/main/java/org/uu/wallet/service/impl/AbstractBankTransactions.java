package org.uu.wallet.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.entity.KycBank;
import org.uu.wallet.service.IKycBankService;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.vo.BankKycTransactionVo;
import org.uu.wallet.vo.KycBankResponseVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 */
@Slf4j
public abstract class AbstractBankTransactions {
    @Resource
    private IKycBankService kycBankService;

    protected String bankCode;

    public KycBankResponseVo linkKycPartnerAccomplish(String kycToken, String bankCode) {
        this.bankCode = bankCode;
        String resStr = request(kycToken);
        KycBankResponseVo kycBankResponseVo = new KycBankResponseVo();
        if (StringUtil.isEmpty(resStr)
                || !JsonUtil.isValidJSONObjectOrArray(resStr)
        ) {
            log.error("连接KYC银行失败, 请求银行接口返回数据为null, resStr: {}", resStr);
            kycBankResponseVo.setMsg(resStr);
            return kycBankResponseVo;
        }
        boolean b = checkResult(resStr);
        if (b) {
            kycBankResponseVo.setStatus(true);
            return kycBankResponseVo;
        }
        kycBankResponseVo.setMsg(resStr);
        return kycBankResponseVo;
    }

    public List<BankKycTransactionVo> getKycBankTransactionAccomplish(String kycToken, String bankCode) {
        this.bankCode = bankCode;
        String resStr = request(kycToken);
        JSONObject resJson = JSONObject.parseObject(resStr);
        List<BankKycTransactionVo> resultList = new ArrayList<>();
        boolean success = checkResult(resStr);
        if (success) {
            filter(resultList, resJson);
        }
        return resultList;
    }

    public String request(String kycToken) {
        KycBank kycBank = kycBankService.getBankInfoByBankCode(bankCode);
        log.info("kyc银行请求接口: url:{}, params:{}", kycBank.getApiUrl(), kycToken);
        String result = RequestUtil.HttpRestClientToJson(kycBank.getApiUrl(), kycToken);
        log.info("kyc银行请求接口: url:{}, params:{}, result:{}", kycBank.getApiUrl(), kycToken, result);
        return result;
    }

    public abstract boolean checkResult(String resultStr);

    public abstract void filter(List<BankKycTransactionVo> resultList, JSONObject resJson) ;
}
