package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreLogsDTO;
import org.uu.common.pay.req.CreditScoreLogsListPageReq;
import org.uu.manager.api.CreditScoreLogsClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(value = {"/api/v1/creditScoreLogsAdmin", "/creditScoreLogsAdmin"})
@Api(description = "信用分记录前端控制器")
public class CreditScoreLogsController {
    private final CreditScoreLogsClient creditScoreLogsClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "信用记录列表")
    public RestResult<List<CreditScoreLogsDTO>> list(@RequestBody @ApiParam CreditScoreLogsListPageReq req) {
        return creditScoreLogsClient.listPage(req);
    }
}
