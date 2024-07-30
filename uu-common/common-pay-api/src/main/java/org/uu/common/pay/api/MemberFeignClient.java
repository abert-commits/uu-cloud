package org.uu.common.pay.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "uu-wallet", contextId = "member")
public interface MemberFeignClient {

    /**
     * 根据会员名称查询会员信息
     * @param username 会员名称
     */
    @GetMapping("/memberInfo/username/{username}")
    RestResult<MemberAuthDTO> getMemberByUsername(@PathVariable String username);
}
