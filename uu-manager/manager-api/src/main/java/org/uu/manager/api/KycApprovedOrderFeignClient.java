package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycApprovedOrderDTO;
import org.uu.common.pay.req.KycApprovedOrderListPageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "kyc-approved-order")
public interface KycApprovedOrderFeignClient {




    @PostMapping("/api/v1/kycApprovedOrder/listPage")
    RestResult<List<KycApprovedOrderDTO>> listPage(@RequestBody KycApprovedOrderListPageReq req);

}
