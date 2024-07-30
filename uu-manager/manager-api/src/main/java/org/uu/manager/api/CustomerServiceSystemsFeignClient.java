package org.uu.manager.api;

import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BannerInfoListPageDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsListPageDTO;
import org.uu.common.pay.dto.CustomerServiceTypesDTO;
import org.uu.common.pay.req.CustomerServiceSystemsReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "customerServiceSystems")
public interface CustomerServiceSystemsFeignClient {

    /**
     * 新增 客服系统
     *
     * @param req
     * @return {@link RestResult}
     */
    @PostMapping("/api/v1/customerService/createCustomerServiceSystems")
    RestResult createCustomerServiceSystems(@RequestBody CustomerServiceSystemsReq req);


    /**
     * 根据ID获取 客服系统信息
     *
     * @param id
     * @return {@link RestResult}<{@link CustomerServiceSystemsDTO}>
     */
    @GetMapping("/api/v1/customerService/{id}")
    RestResult<CustomerServiceSystemsDTO> getCustomerServiceSystemsById(@PathVariable("id") Long id);


    /**
     * 更新 客服系统信息
     *
     * @param id
     * @param req
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/customerService/updateCustomerServiceSystems/{id}")
    RestResult updateCustomerServiceSystems(@PathVariable("id") Long id, @RequestBody CustomerServiceSystemsReq req);


    /**
     * 删除 客服系统信息
     *
     * @param id
     * @return {@link Boolean}
     */
    @DeleteMapping("/api/v1/customerService/{id}")
    RestResult deleteCustomerServiceSystems(@PathVariable("id") Long id);


    /**
     * 禁用 客服系统信息
     *
     * @param id
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/customerService/disable/{id}")
    RestResult disableCustomerServiceSystems(@PathVariable("id") Long id);


    /**
     * 启用 客服系统信息
     *
     * @param id
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/customerService/enable/{id}")
    RestResult enableCustomerServiceSystems(@PathVariable("id") Long id);


    /**
     * 分页查询 客服系统列表
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link PageReturn}<{@link BannerInfoListPageDTO}>>
     */
    @PostMapping("/api/v1/customerService/getCustomerServiceList")
    RestResult<PageReturn<CustomerServiceSystemsListPageDTO>> getCustomerServiceList(@RequestBody(required = false) PageRequest pageRequest);


    /**
     * 获取客服系统类型列表
     *
     * @return {@link RestResult}<{@link List}<{@link CustomerServiceTypesDTO}>>
     */
    @GetMapping("/api/v1/customerService/getCustomerServiceTypes")
    RestResult<List<CustomerServiceTypesDTO>> getCustomerServiceTypes();
}
