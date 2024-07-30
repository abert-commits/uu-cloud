package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MatchPoolDTO;
import org.uu.common.pay.dto.MatchPoolListPageDTO;
import org.uu.common.pay.dto.PaymentOrderChildDTO;
import org.uu.common.pay.dto.PaymentOrderDTO;
import org.uu.common.pay.req.MatchPoolGetChildReq;
import org.uu.common.pay.req.MatchPoolListPageReq;
import org.uu.common.pay.req.MatchPoolReq;
import org.uu.manager.api.MatchPoolClient;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "匹配池配置信息控制器")
@RequestMapping(value = {"/api/v1/matchPoolAdmin", "/matchPoolAdmin"})
public class MatchPoolController {


    private final MatchPoolClient  matchPoolClient;

    @PostMapping("/listpage")
    @ApiOperation(value = "列表")
    public RestResult<List<MatchPoolListPageDTO>> listpage(@RequestBody @ApiParam MatchPoolListPageReq req) {
        RestResult<List<MatchPoolListPageDTO>> result = matchPoolClient.listpage(req);
        return result;
    }


    @PostMapping("/matchPooTotal")
    @ApiOperation(value = "总计")
    public RestResult<MatchPoolListPageDTO> matchPooTotal(@Validated @ApiParam @RequestBody MatchPoolListPageReq req) {
        RestResult<MatchPoolListPageDTO> result = matchPoolClient.matchPooTotal(req);
        return result;
    }

    @PostMapping("/getChildren")
    @ApiOperation(value = "查看子订单")
    public RestResult<List<PaymentOrderChildDTO>> getChildren(@Validated @RequestBody @ApiParam MatchPoolGetChildReq req) {
        RestResult<List<PaymentOrderChildDTO>>  result = matchPoolClient.getChildren(req);
        return result;
    }


}
