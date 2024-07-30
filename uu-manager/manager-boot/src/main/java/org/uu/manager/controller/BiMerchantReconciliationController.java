package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BiMerchantReconciliationDTO;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMerchantReconciliationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商户对账报表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-03-06
 */
@RestController
@RequestMapping(value = {"/api/v1/biMerchantReconciliation", "/biMerchantReconciliation"})
@Api(description = "商户对账报表控制器")
public class BiMerchantReconciliationController {
    @Resource
    IBiMerchantReconciliationService iBiMerchantReconciliationService;

    @PostMapping("/query")
    @ApiOperation(value = "查询商户对账报表记录")
    public RestResult<List<BiMerchantReconciliationDTO>> listPage(@Validated @RequestBody MerchantDailyReportReq req) {

        PageReturn<BiMerchantReconciliationDTO> result = iBiMerchantReconciliationService.listPage(req);
        return RestResult.page(result);
    }
}
