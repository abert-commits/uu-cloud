package org.uu.manager.api;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycPartnersDTO;
import org.uu.common.pay.req.KycPartnerIdReq;
import org.uu.common.pay.req.KycPartnerListPageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.pay.req.KycPartnerMemberReq;
import org.uu.common.pay.req.KycPartnerUpdateReq;

import java.util.List;

/**
 * @author admin
 * @date 2024/4/27 10:26
 */
@FeignClient(value = "uu-wallet", contextId = "kyc-partners")
public interface KycPartnersClient {

    @PostMapping("/api/v1/kycPartners/listPage")
    RestResult<List<KycPartnersDTO>>  listPage(@RequestBody @ApiParam KycPartnerListPageReq kycPartnerListPageReq);

    @PostMapping("/api/v1/kycPartners/delete")
    RestResult delete(@RequestBody @ApiParam KycPartnerIdReq req);

    @PostMapping("/api/v1/kycPartners/updateKycPartner")
    RestResult updateKycPartner(@RequestBody @ApiParam KycPartnerUpdateReq req);

    @PostMapping("/api/v1/kycPartners/getKycPartnerByMemberId")
    RestResult<List<KycPartnersDTO>> getKycPartnerByMemberId(@RequestBody @ApiParam KycPartnerMemberReq req);
}
