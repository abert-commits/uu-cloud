package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.AnnouncementListPageDTO;
import org.uu.common.pay.dto.MerchantRatesConfigDTO;
import org.uu.common.pay.req.MerchantRatesConfigPageReq;
import org.uu.common.pay.req.MerchantRatesConfigReq;

import java.util.List;

/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "merchantRatesConfig")
public interface MerchantRatesConfigFeignClient {


    /**
     * 新增 商户费率设置
     *
     * @param req
     * @return {@link RestResult}
     */
    @PostMapping("/merchantRatesConfig/addMerchantRatesConfig")
    RestResult addMerchantRatesConfig(@RequestBody MerchantRatesConfigReq req);

    /**
     * 根据id获取商户费率设置
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/merchantRatesConfig/getMerchantRatesConfigById/{id}")
    RestResult<MerchantRatesConfigDTO> getMerchantRatesConfigById(@PathVariable("id") Long id);

    /**
     * 更新商户费率设置
     *
     * @param id
     * @param req
     * @return {@link RestResult}
     */
    @PostMapping("/merchantRatesConfig/updateMerchantRatesConfig/{id}")
    RestResult updateMerchantRatesConfig(@PathVariable("id") Long id, @RequestBody MerchantRatesConfigReq req);

    /**
     * 删除商户费率设置
     *
     * @param id
     * @return {@link RestResult}
     */
    @DeleteMapping("/merchantRatesConfig/{id}")
    RestResult deleteMerchantRatesConfig(@PathVariable("id") Long id);


    /**
     * 商户费率设置分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link PageReturn}<{@link AnnouncementListPageDTO}>>
     */
    @PostMapping("/merchantRatesConfig/merchantRatesConfigListPage")
    RestResult<List<MerchantRatesConfigDTO>> merchantRatesConfigListPage(@RequestBody(required = false) MerchantRatesConfigPageReq pageRequest);
}
