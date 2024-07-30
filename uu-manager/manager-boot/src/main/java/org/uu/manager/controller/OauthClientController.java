package org.uu.manager.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.OAuth2ClientDTO;
import org.uu.manager.entity.SysOauthClient;
import org.uu.manager.service.ISysOauthClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping("/api/oauth-clients")
@Slf4j
@AllArgsConstructor
@RestController
@ApiIgnore
public class OauthClientController {
    private ISysOauthClientService iSysOauthClientService;

    @GetMapping("/getOAuth2ClientById")
    public RestResult<OAuth2ClientDTO> getOAuth2ClientById(@RequestParam String clientId) {
        SysOauthClient client = iSysOauthClientService.getById(clientId);
        Assert.notNull(client, "OAuth2 客户端不存在");
        OAuth2ClientDTO oAuth2ClientDTO = new OAuth2ClientDTO();
        BeanUtil.copyProperties(client, oAuth2ClientDTO);
        return RestResult.ok(oAuth2ClientDTO);
    }

}
