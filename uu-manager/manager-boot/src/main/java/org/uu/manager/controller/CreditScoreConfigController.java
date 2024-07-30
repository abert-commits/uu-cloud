package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreConfigDTO;
import org.uu.common.pay.req.CreditScoreConfigListPageReq;
import org.uu.common.pay.req.CreditScoreConfigUpdateReq;
import org.uu.manager.api.CreditScoreConfigClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(value = {"/api/v1/creditScoreConfigAdmin", "/creditScoreConfigAdmin"})
@Api(description = "信用分配置控制器")
public class CreditScoreConfigController {
    private final CreditScoreConfigClient creditScoreConfigClient;


    @PostMapping("/listPage")
    @ApiOperation(value = "查询信用分配置列表")
    public RestResult<List<CreditScoreConfigDTO>> listPage(@RequestBody @ApiParam CreditScoreConfigListPageReq req) {
        return creditScoreConfigClient.listPage(req);
    }

    @PostMapping("/updateScore")
    @ApiOperation(value = "更新信用分配置")
    public RestResult<CreditScoreConfigDTO> updateScore(@RequestBody @ApiParam CreditScoreConfigUpdateReq req) {
        return creditScoreConfigClient.updateScore(req);
    }

}
