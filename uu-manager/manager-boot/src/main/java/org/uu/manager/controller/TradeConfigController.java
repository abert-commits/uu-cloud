package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.TradeConfigClient;

import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "配置管理控制器")
@RequestMapping(value = {"/api/v1/tradeConfigAdmin", "/tradeConfigAdmin"})
public class TradeConfigController {
    private final TradeConfigClient tradeConfigClient;

    @PostMapping("/listpage")
    @ApiOperation(value = "配置管理列表")
    public RestResult list(@RequestBody @ApiParam TradeConfigListPageReq req) {
        RestResult<List<TradeConfigDTO>> result = tradeConfigClient.listpage(req);
        return result;
    }


    @PostMapping("/updateBuy")
    @SysLog(title = "配置管理控制器", content = "参数配置买入")
    @ApiOperation(value = "参数配置买入")
    public RestResult<TradeConfigBuyDTO> updateBuy(@Validated @ApiParam @RequestBody TradeConfigBuyReq req) {
        RestResult<TradeConfigBuyDTO> result = tradeConfigClient.updateBuy(req);
        return result;
    }

    @PostMapping("/updateSell")
    @SysLog(title = "配置管理控制器", content = "参数配置卖出")
    @ApiOperation(value = "参数配置卖出")
    public RestResult<TradeConfigSellDTO> updateSell(@Validated @ApiParam @RequestBody TradeConfigSellReq req) {
        RestResult<TradeConfigSellDTO> result = tradeConfigClient.updateSell(req);
        return result;
    }

    @PostMapping("/updateVoiceEnable")
    @SysLog(title = "配置管理控制器", content = "参数配置语音开关")
    @ApiOperation(value = "参数配置语音开关")
    public RestResult<TradeConfigVoiceEnableDTO> updateVoiceEnable(@Validated @ApiParam @RequestBody TradeConfigVoiceEnableReq req) {
        RestResult<TradeConfigVoiceEnableDTO> result = tradeConfigClient.updateVoiceEnable(req);
        return result;
    }

    @PostMapping("/detaill")
    @ApiOperation(value = "查看配置信息")
    public RestResult<TradeConfigDTO> detaill(@Validated @ApiParam @RequestBody TradeConfigIdReq req) {
        RestResult<TradeConfigDTO> result = tradeConfigClient.detaill(req);
        return result;
    }

    @PostMapping("/delete")
    @SysLog(title = "配置管理控制器", content = "删除")
    @ApiOperation(value = "删除")
    public RestResult delete(@Validated @ApiParam @RequestBody TradeConfigIdReq req) {

        RestResult result = tradeConfigClient.delete(req);
        return result;
    }

    @PostMapping("/updateWarningConfig")
    @ApiOperation(value = "预警参数配置")
    public RestResult<TradeWarningConfigDTO> updateWarningConfig(@Validated @RequestBody TradeConfigWarningConfigUpdateReq req) {
        return tradeConfigClient.updateWarningConfig(req);
    }

    @PostMapping("/warningConfigDetail")
    @ApiOperation(value = "获取预警参数配置")
    public RestResult<TradeWarningConfigDTO> warningConfigDetail(@Validated @RequestBody TradeConfigIdReq req) {
        return tradeConfigClient.warningConfigDetail(req);
    }

    @GetMapping("/dividendConfigList")
    @ApiOperation(value = "平台分红参数配置列表")
    public RestResult<List<DividendConfigDTO>> dividendConfigList() {
        return tradeConfigClient.dividendConfigList();
    }

    @PostMapping("/addDividendConfig")
    @ApiOperation(value = "添加平台分红参数配置")
    public RestResult addDividendConfig(@RequestBody @ApiParam @Valid DividendConfigReq req) {
        return tradeConfigClient.addDividendConfig(req);
    }

    @PostMapping("/updateDividendConfig/{id}")
    @ApiOperation(value = "更新平台分红参数配置")
    public RestResult updateDividendConfig(@PathVariable Long id, @RequestBody @ApiParam @Valid DividendConfigReq req) {
        return tradeConfigClient.updateDividendConfig(id, req);
    }


    @PostMapping("/updateTradeConfig")
    @SysLog(title = "配置管理控制器", content = "更新平台配置参数")
    @ApiOperation(value = "更新平台配置参数")
    public RestResult updateTradeConfig(@Validated @ApiParam @RequestBody TradeConfigUpdateReq req) {
        return tradeConfigClient.updateTradeConfig(req);
    }

}
