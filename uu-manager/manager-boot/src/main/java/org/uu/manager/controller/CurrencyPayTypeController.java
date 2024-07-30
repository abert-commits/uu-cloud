package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CurrencyPayTypeDTO;
import org.uu.common.pay.dto.CurrencyPayTypePageDTO;
import org.uu.common.pay.req.CurrencyPayTypeReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.CurrencyPayTypeFeignClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/currencyPayType")
@Api(description = "币种对应的代收代付类型")
public class CurrencyPayTypeController {
    @Resource
    private CurrencyPayTypeFeignClient currencyPayTypeFeignClient;

    @PostMapping("/currencyPayTypeListById")
    @ApiOperation(value = "币种对应的代收代付类型列表")
    public RestResult<List<CurrencyPayTypeDTO>> currencyPayTypeListById(@RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return currencyPayTypeFeignClient.currencyPayTypeListById(req);
    }


    /**
     * 新增 币种对应的代收代付类型
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/addCurrencyPayType")
    @SysLog(title = "币种对应的代收代付类型管理控制器", content = "新增")
    @ApiOperation(value = "新增币种对应的代收代付类型")
    public RestResult addCurrencyPayType(@RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return currencyPayTypeFeignClient.addCurrencyPayType(req);
    }


    /**
     * 更新 币种对应的代收代付类型
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateCurrency/{id}")
    @SysLog(title = "币种对应的代收代付类型管理控制器", content = "更新")
    @ApiOperation(value = "更新币种对应的代收代付类型")
    public RestResult updateCurrency(@PathVariable Long id, @RequestBody @ApiParam @Valid CurrencyPayTypeReq req) {
        return currencyPayTypeFeignClient.updateCurrencyPayType(id, req);
    }

    /**
     * 删除 币种对应的代收代付类型
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "币种对应的代收代付类型管理控制器", content = "删除")
    @ApiOperation(value = "删除货币")
    public RestResult deleteCurrencyPayType(@PathVariable Long id) {
        return currencyPayTypeFeignClient.deleteCurrencyPayType(id);
    }


    /**
     * 分页查询币种对应的代收代付类型
     */
    @PostMapping("/currencyPage")
    @ApiOperation(value = "分页获取列表 默认获取第一页 20条记录")
    public RestResult<List<CurrencyPayTypePageDTO>> currencyPage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return currencyPayTypeFeignClient.currencyPayTypePage(pageRequest);
    }


}
