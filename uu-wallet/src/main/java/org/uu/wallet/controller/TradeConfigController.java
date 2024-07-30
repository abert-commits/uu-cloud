package org.uu.wallet.controller;


import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.redis.util.RedisUtils;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.service.DividendConfigService;
import org.uu.wallet.service.ITradeConfigService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "配置信息控制器")
@RequestMapping(value = {"/api/v1/tradeConfig", "/tradeConfig"})
@ApiIgnore
public class TradeConfigController {
    private final ITradeConfigService tradeConfigService;
    private final RedisUtils redisUtils;
    private final DividendConfigService dividendConfigService;

    @PostMapping("/listpage")
    @ApiOperation(value = "获取配置列表")
    public RestResult list(@RequestBody @ApiParam TradeConfigListPageReq req) {
        PageReturn<TradeConfigDTO> payConfigPage = tradeConfigService.listPage(req);
        return RestResult.page(payConfigPage);
    }


    @PostMapping("/updateBuy")
    @ApiOperation(value = "参数配置")
    public RestResult updateBuy(@Validated @RequestBody TradeConfigBuyReq req) {
        TradeConfig tradeConfig = new TradeConfig();
        BeanUtils.copyProperties(req, tradeConfig);
        tradeConfig.setIsSplitOrder(null);
        tradeConfigService.updateById(tradeConfig);
        TradeConfigBuyDTO tradeConfigBuyDTO = new TradeConfigBuyDTO();
        BeanUtils.copyProperties(tradeConfig, tradeConfigBuyDTO);
        return RestResult.ok(tradeConfigBuyDTO);
    }

    @PostMapping("/updateTradeConfig")
    @ApiOperation(value = "更新平台配置参数")
    public RestResult updateTradeConfig(@Validated @RequestBody TradeConfigUpdateReq req) {
        TradeConfig tradeConfig = new TradeConfig();
        BeanUtils.copyProperties(req, tradeConfig);
        return tradeConfigService.updateById(tradeConfig) ? RestResult.ok() : RestResult.failed();
    }

    @PostMapping("/updateSell")
    @ApiOperation(value = "卖出参数配置")
    public RestResult<TradeConfigSellDTO> updateSell(@Validated @RequestBody TradeConfigSellReq req) {
        TradeConfig tradeConfig = new TradeConfig();
        BeanUtils.copyProperties(req, tradeConfig);
        tradeConfigService.updateById(tradeConfig);
        redisUtils.set(GlobalConstants.SELL_CONFIG, JSON.toJSON(req));
        TradeConfigSellDTO tradeConfigSellDTO = new TradeConfigSellDTO();
        BeanUtils.copyProperties(tradeConfig, tradeConfigSellDTO);
        return RestResult.ok(tradeConfigSellDTO);
    }

    @PostMapping("/updateVoiceEnable")
    @ApiOperation(value = "语音开关参数配置")
    public RestResult<TradeConfigVoiceEnableDTO> updateVoiceEnable(@Validated @RequestBody TradeConfigVoiceEnableReq req) {
        TradeConfigVoiceEnableDTO result = tradeConfigService.updateVoiceEnable(req);
        return RestResult.ok(result);
    }


    @PostMapping("/detaill")
    @ApiOperation(value = "查看配置信息")
    public RestResult<TradeConfigDTO> detaill(@Validated @RequestBody TradeConfigIdReq req) {
        TradeConfig tradeConfig = tradeConfigService.getById(req.getId());
        TradeConfigDTO tradeConfigDTO = new TradeConfigDTO();
        BeanUtils.copyProperties(tradeConfig, tradeConfigDTO);
        return RestResult.ok(tradeConfigDTO);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "查看配置信息")
    public RestResult delete(@Validated @RequestBody TradeConfigIdReq req) {
        TradeConfig tradeConfig = new TradeConfig();
        BeanUtils.copyProperties(req, tradeConfig);
        tradeConfigService.removeById(tradeConfig);
        return RestResult.ok("删除成功");
    }

    @PostMapping("/updateWarningConfig")
    @ApiOperation(value = "预警参数配置")
    public RestResult<TradeWarningConfigDTO> updateWarningConfig(@Validated @RequestBody TradeConfigWarningConfigUpdateReq req) {
        TradeWarningConfigDTO result = tradeConfigService.updateWarningConfig(req);
        return RestResult.ok(result);
    }

    @PostMapping("/warningConfigDetail")
    @ApiOperation(value = "获取预警参数配置")
    public RestResult<TradeWarningConfigDTO> warningConfigDetail(@Validated @RequestBody TradeConfigIdReq req) {
        TradeWarningConfigDTO result = tradeConfigService.warningConfigDetail(req);
        return RestResult.ok(result);
    }

    @GetMapping("/dividendConfigList")
    @ApiOperation(value = "平台分红参数配置列表")
    public RestResult<List<DividendConfigDTO>> dividendConfigList() {
        List<DividendConfigDTO> dividendConfigDTOS = dividendConfigService.dividendConfigList();
        return RestResult.ok(dividendConfigDTOS);
    }

    @PostMapping("/addDividendConfig")
    @ApiOperation(value = "添加平台分红参数配置")
    public RestResult addDividendConfig(@Validated @RequestBody DividendConfigReq req) {
        return dividendConfigService.addDividendConfig(req);
    }

    @PostMapping("/updateDividendConfig/{id}")
    @ApiOperation(value = "更新平台分红参数配置")
    public RestResult updateDividendConfig(@PathVariable Long id, @Validated @RequestBody DividendConfigReq req) {
        return dividendConfigService.updateDividendConfig(id, req);
    }

}
