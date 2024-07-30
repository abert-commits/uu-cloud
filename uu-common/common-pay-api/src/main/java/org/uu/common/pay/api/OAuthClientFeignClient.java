package org.uu.common.pay.api;


import org.uu.common.core.result.RestResult;

import org.uu.common.pay.dto.OAuth2ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "uu-manager", contextId = "oauth-client")
    public interface OAuthClientFeignClient {

        @GetMapping("/api/oauth-clients/getOAuth2ClientById")
        RestResult<OAuth2ClientDTO> getOAuth2ClientById(@RequestParam String clientId);
    }


