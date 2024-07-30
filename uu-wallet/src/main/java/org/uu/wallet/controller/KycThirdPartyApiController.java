package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.KycRestResult;
import org.uu.wallet.req.KycSendOtpReq;
import org.uu.wallet.req.KycVerifyOtpReq;
import org.uu.wallet.service.IKycThirdPartyApiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.MalformedURLException;

/**
 * @author lukas
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/kycThirdParty")
@Api(value = "KYC第三方接口控制器")
public class KycThirdPartyApiController {
    @Resource
    IKycThirdPartyApiService kycThirdPartyApiService;

    @PostMapping("/sendOtp")
    @ApiOperation(value = "发送kyc短信验证码")
    public KycRestResult<String> sendOtp(@RequestBody @ApiParam KycSendOtpReq req) throws Exception {
        return kycThirdPartyApiService.sendOtp(req);
    }

    @PostMapping("/verifyOtp")
    @ApiOperation(value = "校验kyc短信验证码")
    public KycRestResult<String> verifyOtp(@RequestBody @ApiParam KycVerifyOtpReq req) throws MalformedURLException {
        return kycThirdPartyApiService.verifyOtp(req);
    }
}
