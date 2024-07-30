package org.uu.job.feign;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * @author lukas
 */
@FeignClient(value = "uu-wallet", contextId = "kyc-center")
public interface KycAutoCompleteFeignClient {

    @GetMapping("/kycCenter/pullTransactionJob")
    @ApiOperation(value = "kyc自动完成")
    void autoCompleteTransactionJob();
}
