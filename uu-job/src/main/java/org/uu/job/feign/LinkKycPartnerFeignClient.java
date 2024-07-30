package org.uu.job.feign;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;



@FeignClient(value = "uu-wallet", contextId = "kyc-partners")
public interface LinkKycPartnerFeignClient {
    /**
     * 自动连接kyc
     *
     */
    @GetMapping("/api/v1/kycPartners/link")
    @ApiOperation(value = "自动连接kyc")
    void linKycPartner();
}
