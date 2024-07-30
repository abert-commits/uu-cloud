package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SystemCurrencyDTO;
import org.uu.common.pay.dto.SystemCurrencyPageDTO;
import org.uu.common.pay.req.SystemCurrencyReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.SystemCurrencyFeignClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/systemCurrency")
@Api(description = "货币控制器")
public class SystemCurrencyController {
    @Resource
    private SystemCurrencyFeignClient systemCurrencyFeignClient;

    @PostMapping("/allCurrency")
    @ApiOperation(value = "币种列表")
    public RestResult<List<SystemCurrencyDTO>> allCurrency() {
        return systemCurrencyFeignClient.allCurrency();
    }

    /**
     * 新增 货币
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/addCurrency")
    @SysLog(title = "货币管理控制器", content = "新增")
    @ApiOperation(value = "新增货币")
    public RestResult addCurrency(@RequestBody @ApiParam @Valid SystemCurrencyReq req) {
        return systemCurrencyFeignClient.addCurrency(req);
    }


    /**
     * 更新 货币
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateCurrency/{id}")
    @SysLog(title = "货币管理控制器", content = "更新")
    @ApiOperation(value = "更新货币")
    public RestResult updateCurrency(@PathVariable Long id, @RequestBody @ApiParam @Valid SystemCurrencyReq req) {
        return systemCurrencyFeignClient.updateCurrency(id, req);
    }

    /**
     * 删除 货币
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "货币管理控制器", content = "删除")
    @ApiOperation(value = "删除货币")
    public RestResult deleteCurrency(@PathVariable Long id) {
        return systemCurrencyFeignClient.deleteCurrency(id);
    }


    /**
     * 分页查询货币
     */
    @PostMapping("/currencyPage")
    @ApiOperation(value = "分页获取 货币列表 默认获取第一页 20条记录")
    public RestResult<List<SystemCurrencyPageDTO>> currencyPage(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return systemCurrencyFeignClient.currencyPage(pageRequest);
    }
}
