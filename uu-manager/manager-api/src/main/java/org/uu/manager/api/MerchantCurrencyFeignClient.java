package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantCurrencyDTO;
import org.uu.common.pay.req.MerchantCurrencyReq;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "merchantCurrency")
public interface MerchantCurrencyFeignClient {

    /**
     * 币种列表
     */
    @PostMapping("/merchant-currency/allMerchantCurrency")
    RestResult<List<MerchantCurrencyDTO>> allMerchantCurrency();

    /**
     * 货币分页查询
     */
    @PostMapping("/merchant-currency/merchantCurrencyPage")
    RestResult<List<MerchantCurrencyDTO>> merchantCurrencyPage(@RequestBody PageRequest pageRequest);

    /**
     * 新增 货币
     */
    @PostMapping("/merchant-currency/addMerchantCurrency")
    RestResult addMerchantCurrency(@RequestBody MerchantCurrencyReq req);

    /**
     * 更新 货币
     */
    @PostMapping("/merchant-currency/updateMerchantCurrency/{id}")
    RestResult updateMerchantCurrency(@PathVariable("id") Long id, @RequestBody MerchantCurrencyReq req);

    /**
     * 删除 货币
     */
    @DeleteMapping("/merchant-currency/{id}")
    RestResult deleteMerchantCurrency(@PathVariable("id") Long id);


}
