package org.uu.manager;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BankInfoDTO;
import org.uu.common.pay.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "bank-info")
public interface BankInfoFeignClient {


    @PostMapping("/api/v1/bankInfo/add")
    RestResult<BankInfoDTO> add(@RequestBody BankInfoReq req);


    @PostMapping("/api/v1/bankInfo/detail")
    RestResult<BankInfoDTO> detail(@RequestBody BankInfoIdReq req);

    @PostMapping("/api/v1/bankInfo/listPage")
    RestResult<List<BankInfoDTO>> listPage(@RequestBody BankInfoListPageReq req);

    @PostMapping("/api/v1/bankInfo/update")
    RestResult update(@RequestBody @ApiParam BankInfoUpdateReq req);

    @PostMapping("/api/v1/bankInfo/deleteInfo")
    RestResult deleteInfo(@RequestBody @ApiParam BankInfoIdReq req);

    @PostMapping("/api/v1/bankInfo/updateStatus")
    RestResult updateStatus(@RequestBody @ApiParam BankInfoUpdateStatusReq req);

    @PostMapping("/api/v1/bankInfo/getBankCodeMap")
    RestResult<Map<String, String>> getBankCodeMap();

}
