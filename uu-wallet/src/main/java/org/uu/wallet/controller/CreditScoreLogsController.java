package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreLogsDTO;
import org.uu.common.pay.req.CreditScoreLogsListPageReq;
import org.uu.wallet.req.CreditScoreLogsListReq;
import org.uu.wallet.service.ICreditScoreLogsService;
import org.uu.wallet.vo.CreditScoreLogsVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 信用分记录表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/creditScoreLogs", "/creditScoreLogs"})
@Api(description = "信用分记录前端控制器")
public class CreditScoreLogsController {
    private final ICreditScoreLogsService creditScoreLogsService;

    @PostMapping("/listPage")
    @ApiOperation(value = "信用记录列表")
    @ApiIgnore
    public RestResult<List<CreditScoreLogsDTO>> listPage(@RequestBody @ApiParam CreditScoreLogsListPageReq req) {
        PageReturn<CreditScoreLogsDTO> result = creditScoreLogsService.listPage(req);
        return RestResult.page(result);
    }


    @PostMapping("/list")
    @ApiOperation(value = "前台-信用记录列表")
    public RestResult<PageReturn<CreditScoreLogsVo>> list(@RequestBody @ApiParam CreditScoreLogsListReq req) {
        PageReturn<CreditScoreLogsVo> result = creditScoreLogsService.list(req);
        return RestResult.ok(result);
    }
}
