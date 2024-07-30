package org.uu.common.pay.api;

import org.uu.common.core.result.RestResult;

import org.uu.common.pay.dto.UserAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "uu-manager")
public interface UserFeignClient {

    @GetMapping("/api/v1/users/username/{username}")
    RestResult<UserAuthDTO> getUserByUsername(@PathVariable String username);

    @GetMapping("/api/v1/merchants/username/{username}")
    RestResult<UserAuthDTO> getMerchantByUsername(@PathVariable String username);

    @PostMapping("/syswhite/getIp")
    boolean getIp(@RequestBody Map<String, String> params);


}
