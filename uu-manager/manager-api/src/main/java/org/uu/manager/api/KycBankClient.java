package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycBankDTO;
import org.uu.common.pay.req.KycBankIdReq;
import org.uu.common.pay.req.KycBankListPageReq;
import org.uu.common.pay.req.KycBankReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author admin
 * @date 2024/4/27 11:17
 */
@FeignClient(value = "uu-wallet", contextId = "kyc-bank")
public interface KycBankClient {

    @PostMapping("/api/v1/kycBank/listPage")
    RestResult<List<KycBankDTO>> listPage(KycBankListPageReq req);

    @PostMapping("/api/v1/kycBank/deleteKycBank")
    RestResult deleteKycBank(KycBankIdReq req);

    @PostMapping("/api/v1/kycBank/addKycBank")
    RestResult<KycBankDTO> addKycBank(KycBankReq req);

    @PostMapping("/api/v1/kycBank/updateKycBank")
    RestResult<KycBankDTO> updateKycBank(KycBankReq req);

    @PostMapping("/api/v1/kycBank/getBankCodeList")
    RestResult<List<String>> getBankCodeList();
}
