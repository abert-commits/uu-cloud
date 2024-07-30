package org.uu.wallet.service.impl;

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

import java.util.List;

@Service("bharatpe")
@RequiredArgsConstructor
@Slf4j
public class BharatpeTransactionsImpl implements IAppBankTransaction {

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
        KycBank kycBank = kycBankService.getBankInfoByBankCode("bharatpe");

        // 获取三天记录
        long eDate = System.currentTimeMillis();
        long sDate = eDate - (3 * 24 * 60 * 60 * 1000);
        //将token转为赋值到请求头
        Headers headers = JsonUtil.jsonToHeader(kycToken);

        if (headers == null) {
            kycBankResponseVo.setMsg("Invalid kycToken");
            return kycBankResponseVo;
        }

        JSONObject reqJson = new JSONObject();
        reqJson.put("sDate",sDate);
        reqJson.put("eDate",eDate);

        //请求银行, 根据银行返回结果判断是否连接成功
        String resStr = RequestUtil.getForAppJson(kycBank.getApiUrl(), reqJson, headers);

        if (StringUtil.isEmpty(resStr) || !JsonUtil.isValidJSONObjectOrArray(resStr)) {
            log.error("连接KYC银行失败, 请求银行接口返回数据为null, resStr: {}", resStr);

            kycBankResponseVo.setMsg(resStr);
            return kycBankResponseVo;
        }

        JSONObject resJson = JSONObject.parseObject(resStr);

        Boolean success = resJson.getBoolean("status");

        if (success != null && success) {
            kycBankResponseVo.setStatus(true);
            return kycBankResponseVo;
        }

        kycBankResponseVo.setMsg(resStr);
        return kycBankResponseVo;
    }


    /**
     * 获取 KYC银行交易记录
     *
     * @return {@link Boolean}
     */
    @Override
    public List<BankKycTransactionVo> getKYCBankTransactions(String kycToken) {
        return null;
    }
}
