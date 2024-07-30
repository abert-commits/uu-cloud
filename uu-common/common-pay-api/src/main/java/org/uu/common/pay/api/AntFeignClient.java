package org.uu.common.pay.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AntInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "ar-sixNinePay", contextId = "69pay-ant")
public interface AntFeignClient {
    @GetMapping("/antInfo/antName/{antName}")
    RestResult<AntInfoDTO> getAntByAntName(@PathVariable String antName);

    @PostMapping("/syswhite/getIp")
    boolean getIp(@RequestBody Map<String, String> params);
}
