package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.wallet.entity.C2cConfig;
import org.uu.wallet.entity.CancellationRecharge;
import org.uu.wallet.req.C2cConfigReq;
import org.uu.wallet.req.CancellationRechargeReq;
import org.uu.wallet.service.IC2cConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;

/**
* @author 
*/  @Slf4j
    @RestController
    @RequiredArgsConstructor
    @Api(description = "c2c配置信息控制器")
    @RequestMapping(value = {"/api/v1/c2cConfig", "/c2cConfig"})
    @ApiIgnore
    public class C2cConfigController {
        private final IC2cConfigService   c2cConfigService;

    @PostMapping("/listpage")
    @ApiOperation(value = "获取配置列表")
    public RestResult list(@RequestBody @ApiParam C2cConfigReq c2cConfigReq) {
        PageReturn<C2cConfigDTO> payConfigPage = c2cConfigService.listPage(c2cConfigReq);
        return RestResult.page(payConfigPage);
    }


    @PostMapping("/update")
    @ApiOperation(value = "更新配置信息")
    public RestResult updateC2cConfig(@Validated @RequestBody C2cConfigReq req) {
        C2cConfig c2cConfig = new C2cConfig();
        BeanUtils.copyProperties(req, c2cConfig);
        c2cConfigService.updateById(c2cConfig);
        return RestResult.ok(c2cConfig);
    }

    @PostMapping("/detaill")
    @ApiOperation(value = "查看配置信息")
    public RestResult getC2cConfig(@Validated @RequestBody C2cConfigReq req) {
       // C2cConfig c2cConfig = new C2cConfig();
       // BeanUtils.copyProperties(req, c2cConfig);
        C2cConfig c2cConfig  = c2cConfigService.getById(req.getId());
        return RestResult.ok(c2cConfig);
    }



    }
