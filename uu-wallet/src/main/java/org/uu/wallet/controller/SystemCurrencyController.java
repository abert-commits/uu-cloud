package org.uu.wallet.controller;


import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SystemCurrencyDTO;
import org.uu.common.pay.dto.SystemCurrencyPageDTO;
import org.uu.common.pay.req.SystemCurrencyReq;
import org.uu.wallet.service.ISystemCurrencyService;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 货币配置表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@RestController
@RequestMapping("/system-currency")
public class SystemCurrencyController {
    @Resource
    private ISystemCurrencyService systemCurrencyService;

    /**
     * 商户费率设置分页查询
     */
    @ApiIgnore
    @PostMapping("/allCurrency")
    public RestResult<List<SystemCurrencyDTO>> allCurrency() {
        return RestResult.ok(systemCurrencyService.allCurrency());
    }


    /**
     * 首页货币分页查询
     */
    @ApiIgnore
    @PostMapping("/currencyPage")
    public RestResult<List<SystemCurrencyPageDTO>> currencyPage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        PageReturn<SystemCurrencyPageDTO> pageReturn = systemCurrencyService.currencyPage(pageRequest);
        return RestResult.page(pageReturn);
    }


    /**
     * 新增 货币
     */
    @PostMapping("/addCurrency")
    @ApiIgnore
    public RestResult addCurrency(@RequestBody @ApiParam @Valid SystemCurrencyReq req) {
        return systemCurrencyService.addCurrency(req);
    }


    /**
     * 更新 货币
     */
    @PostMapping("/updateCurrency/{id}")
    @ApiIgnore
    public RestResult updateCurrency(@PathVariable Long id, @RequestBody @ApiParam @Valid SystemCurrencyReq req) {
        return systemCurrencyService.updateSystemCurrency(id, req);
    }

    /**
     * 删除 货币
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteCurrency(@PathVariable Long id) {
        return systemCurrencyService.deleteSystemCurrency(id) ? RestResult.ok() : RestResult.failed();
    }


}
