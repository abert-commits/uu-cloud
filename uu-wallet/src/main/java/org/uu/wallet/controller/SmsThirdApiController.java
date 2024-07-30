package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SmsBalanceWarnDTO;
import org.uu.wallet.thirdParty.TelephoneClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * @author
 */
@RestController
@RequestMapping(value = {"/api/v1/smsThirdApi", "/smsThirdApi"})
@ApiIgnore
public class SmsThirdApiController {

    @Resource
    private TelephoneClient telephoneClient;

    @GetMapping("/checkBalance")
    @ApiOperation(value = "监控短信账户余额")
    public RestResult<SmsBalanceWarnDTO> checkBalance() {
        return RestResult.ok(telephoneClient.checkBalance());
    }



}
