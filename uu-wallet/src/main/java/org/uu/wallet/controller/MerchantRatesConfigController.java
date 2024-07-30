package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantRatesConfigDTO;
import org.uu.common.pay.req.MerchantRatesConfigPageReq;
import org.uu.common.pay.req.MerchantRatesConfigReq;
import org.uu.wallet.service.IMerchantRatesConfigService;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author afei
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "MerchantRatesConfig商户对应的费率设置")
@RequestMapping(value = {"/merchantRatesConfig"})
@Validated
public class MerchantRatesConfigController {


    @Resource
    private IMerchantRatesConfigService merchantRatesConfigService;

    /**
     * 商户费率设置分页查询
     */
    @ApiIgnore
    @PostMapping("/merchantRatesConfigListPage")
    public RestResult<List<MerchantRatesConfigDTO>> merchantRatesConfigListPage(@RequestBody(required = false) @ApiParam @Valid MerchantRatesConfigPageReq pageRequest) {
        PageReturn<MerchantRatesConfigDTO> page = merchantRatesConfigService.merchantRatesConfigListPage(pageRequest);
        return RestResult.page(page);
    }


    /**
     * 新增 商户费率设置
     */
    @PostMapping("/addMerchantRatesConfig")
    @ApiIgnore
    public RestResult addMerchantRatesConfig(@RequestBody @ApiParam @Valid MerchantRatesConfigReq req) {
        return merchantRatesConfigService.addMerchantRatesConfig(req);
    }

    /**
     * 根据ID查询商户费率设置
     */
    @GetMapping("/getMerchantRatesConfigById/{id}")
    @ApiIgnore
    public RestResult<MerchantRatesConfigDTO> getMerchantRatesConfigById(@PathVariable Long id) {
        return merchantRatesConfigService.getMerchantRatesConfigById(id);
    }

    /**
     * 更新 商户费率设置
     */
    @PostMapping("/updateMerchantRatesConfig/{id}")
    @ApiIgnore
    public RestResult updateMerchantRatesConfig(@PathVariable Long id, @RequestBody @ApiParam @Valid MerchantRatesConfigReq req) {
        return merchantRatesConfigService.updateMerchantRatesConfig(id, req);
    }

    /**
     * 删除 商户费率设置
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteMerchantRatesConfig(@PathVariable Long id) {
        return merchantRatesConfigService.deleteMerchantRatesConfig(id) ? RestResult.ok() : RestResult.failed();
    }
}
