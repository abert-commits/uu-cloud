package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SystemCurrencyDTO;
import org.uu.common.pay.dto.SystemCurrencyPageDTO;
import org.uu.common.pay.req.SystemCurrencyReq;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "systemCurrency")
public interface SystemCurrencyFeignClient {

    /**
     * 币种列表
     */
    @PostMapping("/system-currency/allCurrency")
    RestResult<List<SystemCurrencyDTO>> allCurrency();

    /**
     * 货币分页查询
     */
    @PostMapping("/system-currency/currencyPage")
    RestResult<List<SystemCurrencyPageDTO>> currencyPage(@RequestBody PageRequest pageRequest);

    /**
     * 新增 货币
     */
    @PostMapping("/system-currency/addCurrency")
    RestResult addCurrency(@RequestBody SystemCurrencyReq req);

    /**
     * 更新 货币
     */
    @PostMapping("/system-currency/updateCurrency/{id}")
    RestResult updateCurrency(@PathVariable("id") Long id, @RequestBody SystemCurrencyReq req);

    /**
     * 删除 货币
     */
    @DeleteMapping("/system-currency/{id}")
    RestResult deleteCurrency(@PathVariable("id") Long id);


}
