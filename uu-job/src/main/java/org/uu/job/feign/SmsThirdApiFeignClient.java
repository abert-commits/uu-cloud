package org.uu.job.feign;

import io.swagger.annotations.ApiOperation;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SmsBalanceWarnDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "uu-wallet", contextId = "sms-third-api")
public interface SmsThirdApiFeignClient {

    @GetMapping("/api/v1/smsThirdApi/checkBalance")
    @ApiOperation(value = "监控短信账户余额")
    RestResult<SmsBalanceWarnDTO> checkBalance();
}
