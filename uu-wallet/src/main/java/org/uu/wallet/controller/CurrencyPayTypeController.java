package org.uu.wallet.controller;


import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CurrencyPayTypeDTO;
import org.uu.common.pay.dto.CurrencyPayTypePageDTO;
import org.uu.common.pay.req.CurrencyPayTypeReq;
import org.uu.wallet.service.ICurrencyPayTypeService;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 币种对应的代收代付类型 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@RestController
@RequestMapping("/currency-pay-type")
public class CurrencyPayTypeController {
    @Resource
    private ICurrencyPayTypeService currencyPayTypeService;

    /**
     * 币种对应的代收代付类型列表
     */
    @ApiIgnore
    @PostMapping("/currencyPayTypeListById")
    public RestResult<List<CurrencyPayTypeDTO>> currencyPayTypeListById(@RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return RestResult.ok(currencyPayTypeService.currencyPayTypeListById(req.getCurrencyId(), req.getType()));
    }


    /**
     * 币种对应的代收代付分页查询
     */
    @ApiIgnore
    @PostMapping("/currencyPayTypePage")
    public RestResult<List<CurrencyPayTypePageDTO>> currencyPayTypePage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        PageReturn<CurrencyPayTypePageDTO> pageReturn = currencyPayTypeService.currencyPayTypePage(pageRequest);
        return RestResult.page(pageReturn);
    }


    /**
     * 新增 币种对应的代收代付
     */
    @PostMapping("/addCurrencyPayType")
    @ApiIgnore
    public RestResult addCurrencyPayType(@RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return currencyPayTypeService.addCurrencyPayType(req);
    }


    /**
     * 更新 币种对应的代收代付
     */
    @PostMapping("/updateCurrencyPayType/{id}")
    @ApiIgnore
    public RestResult updateCurrencyPayType(@PathVariable Long id, @RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return currencyPayTypeService.updateCurrencyPayType(id, req);
    }

    /**
     * 删除 币种对应的代收代付
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteCurrencyPayType(@PathVariable Long id) {
        return currencyPayTypeService.deleteCurrencyPayType(id) ? RestResult.ok() : RestResult.failed();
    }


}
