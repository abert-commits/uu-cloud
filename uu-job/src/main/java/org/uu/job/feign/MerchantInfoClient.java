package org.uu.job.feign;

import org.uu.common.pay.dto.MerchantLastOrderWarnDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "merchant-info")
public interface MerchantInfoClient {
    @PostMapping("/api/v1/merchantinfo/getLatestOrderTime")
    List<MerchantLastOrderWarnDTO> getLatestOrderTime();
}
