package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantInfoReportDTO;
import org.uu.common.pay.req.MerchantInfoReq;
import org.uu.wallet.service.IReportMerchantInfoService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController

@RequestMapping(value = {"/api/v1/reportMerchantInfo", "/reportMerchantInfo"})
@Api(description = "商户控制器")
@ApiIgnore
public class ReportMerchantInfoController {

    private final IReportMerchantInfoService reportMerchantInfoService;
    @PostMapping("/listpage")
    @ApiOperation(value = "获取商户列表")
    public RestResult<List<MerchantInfoReportDTO>> list(@RequestBody @ApiParam MerchantInfoReq merchantInfoReq) {
        PageReturn<MerchantInfoReportDTO> payConfigPage = reportMerchantInfoService.listDayPage(merchantInfoReq);
        return RestResult.page(payConfigPage);
    }


}
