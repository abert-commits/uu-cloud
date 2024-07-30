package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreConfigDTO;
import org.uu.common.pay.req.CreditScoreConfigListPageReq;
import org.uu.common.pay.req.CreditScoreConfigUpdateReq;
import org.uu.wallet.service.ICreditScoreConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 信用分配置表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/creditScoreConfig", "/creditScoreConfig"})
@Api(description = "信用分配置控制器")
@ApiIgnore
public class CreditScoreConfigController {
    private final ICreditScoreConfigService creditScoreConfigService;


    @PostMapping("/listPage")
    @ApiOperation(value = "查询信用分配置列表")
    public RestResult<List<CreditScoreConfigDTO>> listPage(@RequestBody @ApiParam CreditScoreConfigListPageReq req) {
        PageReturn<CreditScoreConfigDTO> payConfigPage = creditScoreConfigService.listPage(req);
        return RestResult.page(payConfigPage);
    }

    @PostMapping("/updateScore")
    @ApiOperation(value = "更新信用分配置")
    public RestResult<CreditScoreConfigDTO> updateScore(@RequestBody @ApiParam CreditScoreConfigUpdateReq req) {
        return creditScoreConfigService.updateScore(req);
    }

}
