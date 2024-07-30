package org.uu.wallet.controller;


import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantCurrencyDTO;
import org.uu.common.pay.req.MerchantCurrencyReq;
import org.uu.wallet.service.IMerchantCurrencyService;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 商户货币配置表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@RestController
@RequestMapping("/merchant-currency")
public class MerchantCurrencyController {
    @Resource
    private IMerchantCurrencyService merchantCurrencyService;

    /**
     * 商户货币分页查询
     */
    @ApiIgnore
    @PostMapping("/allMerchantCurrency")
    public RestResult<List<MerchantCurrencyDTO>> allMerchantCurrency() {
        return RestResult.ok(merchantCurrencyService.allCurrency());
    }


    /**
     * 商户货币分页查询
     */
    @ApiIgnore
    @PostMapping("/merchantCurrencyPage")
    public RestResult<List<MerchantCurrencyDTO>> merchantCurrencyPage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        PageReturn<MerchantCurrencyDTO> pageReturn = merchantCurrencyService.currencyPage(pageRequest);
        return RestResult.page(pageReturn);
    }


    /**
     * 新增 商户货币
     */
    @PostMapping("/addMerchantCurrency")
    @ApiIgnore
    public RestResult addMerchantCurrency(@RequestBody @ApiParam @Valid MerchantCurrencyReq req) {
        return merchantCurrencyService.addCurrency(req);
    }


    /**
     * 更新 商户货币
     */
    @PostMapping("/updateMerchantCurrency/{id}")
    @ApiIgnore
    public RestResult updateMerchantCurrency(@PathVariable Long id, @RequestBody @ApiParam @Valid MerchantCurrencyReq req) {
        return merchantCurrencyService.updateSystemCurrency(id, req);
    }

    /**
     * 删除 商户货币
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteMerchantCurrency(@PathVariable Long id) {
        return merchantCurrencyService.deleteSystemCurrency(id) ? RestResult.ok() : RestResult.failed();
    }


}
