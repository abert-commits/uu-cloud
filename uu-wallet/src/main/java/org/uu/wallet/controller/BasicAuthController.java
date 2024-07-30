package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.req.BasicAuthReq;
import org.uu.wallet.service.IMemberInfoService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/BasicAuth"})
@Api(description = "BasicAuth控制器")
public class BasicAuthController {

    private final IMemberInfoService memberInfoService;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/getBasicAuth")
    @ApiOperation(value = "获取BasicAuth")
    public RestResult getBasicAuth(@RequestBody @ApiParam @Valid BasicAuthReq basicAuthReq) {

        // 生成 Basic 认证
        String auth = basicAuthReq.getUsername() + ":" + basicAuthReq.getPassword();
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        return RestResult.ok(basicAuth);
    }
}
