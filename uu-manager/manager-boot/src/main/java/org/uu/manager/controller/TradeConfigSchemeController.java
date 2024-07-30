package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TradeConfigSchemeDTO;
import org.uu.common.pay.req.*;
import org.uu.manager.api.TradeConfigSchemeClient;
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
@Api(description = "配置管理方案控制器")
@RequestMapping(value = {"/api/v1/tradeConfigSchemeAdmin", "/tradeConfigSchemeAdmin"})
public class TradeConfigSchemeController {
    private final TradeConfigSchemeClient tradeConfigSchemeClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "配置管理列表")
    public RestResult<List<TradeConfigSchemeDTO>> list(@RequestBody @ApiParam TradeConfigSchemeListPageReq req) {
        return tradeConfigSchemeClient.listPage(req);
    }

    @PostMapping("/detail")
    @ApiOperation(value = "查看配置信息")
    public RestResult<TradeConfigSchemeDTO> detail(@ApiParam @RequestBody TradeConfigIdReq req) {
        return tradeConfigSchemeClient.detail(req);
    }

    @PostMapping("/updateScheme")
    @ApiOperation(value = "查看配置信息")
    public RestResult<TradeConfigSchemeDTO> update(@ApiParam @RequestBody TradeConfigSchemeReq req) {
        return tradeConfigSchemeClient.updateScheme(req);
    }

}
