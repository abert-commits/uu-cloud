package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CurrencyPayTypeDTO;
import org.uu.common.pay.dto.CurrencyPayTypePageDTO;
import org.uu.common.pay.req.CurrencyPayTypeReq;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "currencyPayType")
public interface CurrencyPayTypeFeignClient {


    /**
     * 币种对应的代收代付类型列表
     */
    @PostMapping("/currency-pay-type/currencyPayTypeListById")
    RestResult<List<CurrencyPayTypeDTO>> currencyPayTypeListById(@RequestBody CurrencyPayTypeReq req);


    /**
     * 货币分页查询
     */
    @PostMapping("/currency-pay-type/currencyPayTypePage")
    RestResult<List<CurrencyPayTypePageDTO>> currencyPayTypePage(@RequestBody PageRequest pageRequest);

    /**
     * 新增 货币
     */
    @PostMapping("/currency-pay-type/addCurrencyPayType")
    RestResult addCurrencyPayType(@RequestBody CurrencyPayTypeReq req);

    /**
     * 更新 货币
     */
    @PostMapping("/currency-pay-type/updateCurrencyPayType/{id}")
    RestResult updateCurrencyPayType(@PathVariable("id") Long id, @RequestBody CurrencyPayTypeReq req);

    /**
     * 删除 货币
     */
    @DeleteMapping("/currency-pay-type/{id}")
    RestResult deleteCurrencyPayType(@PathVariable("id") Long id);
}
