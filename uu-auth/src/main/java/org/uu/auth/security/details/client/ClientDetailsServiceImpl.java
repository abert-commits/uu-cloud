package org.uu.auth.security.details.client;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;

import lombok.RequiredArgsConstructor;
import org.uu.auth.comm.enums.PasswordEncoderTypeEnum;
import org.uu.common.core.result.RestResult;

import org.uu.common.pay.api.OAuthClientFeignClient;
import org.uu.common.pay.dto.OAuth2ClientDTO;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientDetailsServiceImpl implements ClientDetailsService {
    private final OAuthClientFeignClient oAuthClientFeignClient;

    @Override
    public ClientDetails loadClientByClientId(String clientId) {
        // 通过feign 调用admin服务获取client信息
        RestResult<OAuth2ClientDTO> result = oAuthClientFeignClient.getOAuth2ClientById(clientId);
        if (RestResult.ok().getCode().equals(result.getCode())) {
            OAuth2ClientDTO client = result.getData();
            BaseClientDetails clientDetails = new BaseClientDetails(
                    client.getClientId(),
                    client.getResourceIds(),
                    client.getScope(),
                    client.getAuthorizedGrantTypes(),
                    client.getAuthorities(),
                    client.getWebServerRedirectUri());
            clientDetails.setClientSecret(PasswordEncoderTypeEnum.NOOP.getPrefix() + client.getClientSecret());
            clientDetails.setAccessTokenValiditySeconds(client.getAccessTokenValidity());
            clientDetails.setRefreshTokenValiditySeconds(client.getRefreshTokenValidity());
            return clientDetails;
        } else {
            throw new NoSuchClientException(result.getMsg());
        }
    }
}
