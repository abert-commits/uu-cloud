package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantCurrencyDTO;
import org.uu.common.pay.req.MerchantCurrencyReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MerchantCurrencyFeignClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/merchantCurrency")
@Api(description = "商户货币控制器")
public class MerchantCurrencyController {
    @Resource
    private MerchantCurrencyFeignClient merchantCurrencyFeignClient;

    @PostMapping("/allCurrency")
    @ApiOperation(value = "商户币种列表")
    public RestResult<List<MerchantCurrencyDTO>> allCurrency() {
        return merchantCurrencyFeignClient.allMerchantCurrency();
    }

    /**
     * 新增 商户货币
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/addCurrency")
    @SysLog(title = "商户货币管理控制器", content = "新增")
    @ApiOperation(value = "新增商户货币")
    public RestResult addCurrency(@RequestBody @ApiParam @Valid MerchantCurrencyReq req) {
        return merchantCurrencyFeignClient.addMerchantCurrency(req);
    }


    /**
     * 更新 商户货币
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateCurrency/{id}")
    @SysLog(title = "商户货币管理控制器", content = "更新")
    @ApiOperation(value = "更新商户货币")
    public RestResult updateCurrency(@PathVariable Long id, @RequestBody @ApiParam @Valid MerchantCurrencyReq req) {
        return merchantCurrencyFeignClient.updateMerchantCurrency(id, req);
    }

    /**
     * 删除 商户货币
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "商户货币管理控制器", content = "删除")
    @ApiOperation(value = "删除商户货币")
    public RestResult deleteCurrency(@PathVariable Long id) {
        return merchantCurrencyFeignClient.deleteMerchantCurrency(id);
    }


    /**
     * 分页查询商户货币
     */
    @PostMapping("/currencyPage")
    @ApiOperation(value = "分页获取 商户货币列表 默认获取第一页 20条记录")
    public RestResult<List<MerchantCurrencyDTO>> currencyPage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return merchantCurrencyFeignClient.merchantCurrencyPage(pageRequest);
    }
}
