package org.uu.wallet.controller;


import io.swagger.annotations.ApiParam;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.CustomerServiceSystemsReq;
import org.uu.wallet.service.ICustomerServiceSystemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 客服系统配置表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@RestController
@RequestMapping(value = {"/api/v1/customerService", "/customerService"})
@ApiIgnore
public class CustomerServiceSystemsController {

    @Autowired
    private ICustomerServiceSystemsService customerServiceSystemsService;

    /**
     * 新增 客服系统
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createCustomerServiceSystems")
    @ApiIgnore
    public RestResult createCustomerServiceSystems(@RequestBody @ApiParam @Valid CustomerServiceSystemsReq req) {
        return customerServiceSystemsService.createCustomerServiceSystems(req);
    }

    /**
     * 根据ID查询客服系统信息
     *
     * @param id
     * @return {@link RestResult}<{@link BannerInfoDTO}>
     */
    @GetMapping("/{id}")
    @ApiIgnore
    public RestResult<CustomerServiceSystemsDTO> getCustomerServiceSystemsById(@PathVariable Long id) {
        return customerServiceSystemsService.getCustomerServiceSystemsById(id);
    }

    /**
     * 更新 客服系统
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateCustomerServiceSystems/{id}")
    @ApiIgnore
    public RestResult updateCustomerServiceSystems(@PathVariable Long id, @RequestBody @ApiParam @Valid CustomerServiceSystemsReq req) {
        return customerServiceSystemsService.updateCustomerServiceSystems(id, req);
    }

    /**
     * 删除 客服系统
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteCustomerServiceSystems(@PathVariable Long id) {
        return customerServiceSystemsService.deleteCustomerServiceSystems(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 禁用 客服系统
     *
     * @param id
     * @return boolean
     */
    @ApiIgnore
    @PostMapping("/disable/{id}")
    public RestResult disableCustomerServiceSystems(@PathVariable Long id) {
        return customerServiceSystemsService.disableCustomerServiceSystems(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 启用 客服系统
     *
     * @param id
     * @return {@link RestResult}
     */
    @ApiIgnore
    @PostMapping("/enable/{id}")
    public RestResult enableCustomerServiceSystems(@PathVariable Long id) {
        return customerServiceSystemsService.enableCustomerServiceSystems(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link BannerInfoListPageDTO}>>
     */
    @ApiIgnore
    @PostMapping("/getCustomerServiceList")
    public RestResult<PageReturn<CustomerServiceSystemsListPageDTO>> getCustomerServiceList(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return customerServiceSystemsService.listPage(pageRequest);
    }

    /**
     * 获取客服系统类型列表
     *
     * @return {@link RestResult}<{@link List}<{@link CustomerServiceTypesDTO}>>
     */
    @GetMapping("/getCustomerServiceTypes")
    public RestResult<List<CustomerServiceTypesDTO>> getCustomerServiceTypes() {
        return customerServiceSystemsService.getCustomerServiceTypes();
    }
}
