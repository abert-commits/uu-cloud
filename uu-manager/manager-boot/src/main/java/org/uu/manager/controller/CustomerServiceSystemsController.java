package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BannerInfoListPageDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsListPageDTO;
import org.uu.common.pay.dto.CustomerServiceTypesDTO;
import org.uu.common.pay.req.CustomerServiceSystemsReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.CustomerServiceSystemsFeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 客服系统 前端控制器
 * </p>
 *
 * @author
 * @since 2024-04-21
 */
@RestController
@RequestMapping("/customerService")
@Api(description = "客服系统管理控制器")
@Validated
@RequiredArgsConstructor
public class CustomerServiceSystemsController {

    private final CustomerServiceSystemsFeignClient customerServiceSystemsFeignClient;

    /**
     * 新增 客服系统
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createCustomerServiceSystems")
    @ApiOperation(value = "新增 客服系统")
    @SysLog(title = "客服系统管理控制器", content = "新增")
    public RestResult createCustomerServiceSystems(@RequestBody @ApiParam @Valid CustomerServiceSystemsReq req) {
        return customerServiceSystemsFeignClient.createCustomerServiceSystems(req);
    }

    /**
     * 根据ID查询客服系统信息
     *
     * @param id
     * @return {@link RestResult}<{@link CustomerServiceSystemsDTO}>
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询客服系统信息")
    public RestResult<CustomerServiceSystemsDTO> getCustomerServiceSystemsById(@PathVariable Long id) {
        return customerServiceSystemsFeignClient.getCustomerServiceSystemsById(id);
    }

    /**
     * 更新 Banner
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateCustomerServiceSystems/{id}")
    @ApiOperation(value = "更新 客服系统信息")
    @SysLog(title = "客服系统管理控制器", content = "更新")
    public RestResult updateBanner(@PathVariable Long id, @RequestBody @ApiParam @Valid CustomerServiceSystemsReq req) {
        return customerServiceSystemsFeignClient.updateCustomerServiceSystems(id, req);
    }

    /**
     * 删除 Banner
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除 客服系统")
    @SysLog(title = "客服系统管理控制器", content = "删除")
    public RestResult deleteBanner(@PathVariable Long id) {
        return customerServiceSystemsFeignClient.deleteCustomerServiceSystems(id);
    }

    /**
     * 禁用 Banner
     *
     * @param id
     * @return boolean
     */
    @PostMapping("/disable/{id}")
    @ApiOperation(value = "禁用 客服系统")
    @SysLog(title = "客服系统管理控制器", content = "禁用")
    public RestResult disableBanner(@PathVariable Long id) {
        return customerServiceSystemsFeignClient.disableCustomerServiceSystems(id);
    }

    /**
     * 启用 Banner
     *
     * @param id
     * @return {@link RestResult}
     */
    @PostMapping("/enable/{id}")
    @ApiOperation(value = "启用 客服系统")
    @SysLog(title = "客服系统管理控制器", content = "启用")
    public RestResult enableBanner(@PathVariable Long id) {
        return customerServiceSystemsFeignClient.enableCustomerServiceSystems(id);
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link BannerInfoListPageDTO}>>
     */
    @PostMapping("/getCustomerServiceList")
    @ApiOperation(value = "分页获取 客服系统列表 默认获取第一页 20条记录")
    public RestResult<PageReturn<CustomerServiceSystemsListPageDTO>> listBanners(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return customerServiceSystemsFeignClient.getCustomerServiceList(pageRequest);
    }


    /**
     * 获取客服系统类型列表
     *
     * @return {@link RestResult}<{@link List}<{@link CustomerServiceTypesDTO}>>
     */
    @GetMapping("/getCustomerServiceTypes")
    @ApiOperation(value = "获取客服系统类型列表")
    public RestResult<List<CustomerServiceTypesDTO>> getCustomerServiceTypes() {
        return customerServiceSystemsFeignClient.getCustomerServiceTypes();
    }
}
