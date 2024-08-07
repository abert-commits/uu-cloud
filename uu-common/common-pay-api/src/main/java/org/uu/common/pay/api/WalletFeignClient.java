package org.uu.common.pay.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UserAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "uu-wallet")
public interface WalletFeignClient {



    @GetMapping("/api/v1/users/merchant/username/{username}")
    RestResult<UserAuthDTO> getMerchantByUsername(@PathVariable String username);
}
