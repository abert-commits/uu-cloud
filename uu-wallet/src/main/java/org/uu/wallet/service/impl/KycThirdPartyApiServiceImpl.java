package org.uu.wallet.service.impl;

import org.uu.common.core.result.KycRestResult;
import org.uu.wallet.req.KycSendOtpReq;
import org.uu.wallet.req.KycVerifyOtpReq;
import org.uu.wallet.service.IKycThirdPartyApiService;
import org.uu.wallet.util.SpringContextUtil;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

/**
 * @author lukas
 */
@Service
public class KycThirdPartyApiServiceImpl implements IKycThirdPartyApiService {
    @Override
    public KycRestResult<String> sendOtp(KycSendOtpReq req) throws Exception {
        String bankCode = req.getBankCode();
        IKycThirdPartyTransaction appBankTransaction = getBean(bankCode);
        return appBankTransaction.sendOtp(req);
    }

    @Override
    public KycRestResult<String> verifyOtp(KycVerifyOtpReq req) throws MalformedURLException {
        String bankCode = req.getBankCode();
        IKycThirdPartyTransaction appBankTransaction = getBean(bankCode);
        return appBankTransaction.verifyOtp(req);
    }

    /**
     * 根据bankCode获取对应实现service
     * @param bankCode bankCode
     * @return {@link IKycThirdPartyTransaction}
     */
    private IKycThirdPartyTransaction getBean(String bankCode) {
        String beanName = bankCode + "ThirdPartyApi";
        return SpringContextUtil.getBean(beanName);
    }
}
