package org.uu.common.pay.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "uu-wallet", contextId = "memberApp")
public interface AppMemberFeignClient {

    @GetMapping("/api/v1/memberInfo/appusername/{username}")
    RestResult<MemberAuthDTO> getAppMemberByUsername(@PathVariable String username);

}
