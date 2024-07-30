package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantRatesConfigDTO;
import org.uu.common.pay.req.MerchantRatesConfigPageReq;
import org.uu.common.pay.req.MerchantRatesConfigReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MerchantRatesConfigFeignClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * MerchantRatesConfig商户对应的费率设置
 * </p>
 */
@RestController
@RequestMapping("/merchantRatesConfig")
@Validated
@RequiredArgsConstructor
@Api(description = "商户费率设置管理控制器")
public class MerchantRatesConfigController {

    @Resource
    private MerchantRatesConfigFeignClient merchantRatesConfigFeignClient;


    /**
     * 新增 商户费率设置
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/addMerchantRatesConfig")
    @SysLog(title = "商户费率设置管理控制器", content = "新增")
    @ApiOperation(value = "新增商户费率设置")
    public RestResult addMerchantRatesConfig(@RequestBody @ApiParam @Valid MerchantRatesConfigReq req) {
        return merchantRatesConfigFeignClient.addMerchantRatesConfig(req);
    }

    /**
     * 根据ID查询商户费率设置
     *
     * @param id
     */
    @GetMapping("/getMerchantRatesConfigById/{id}")
    @ApiOperation(value = "根据ID查询商户费率设置")
    public RestResult<MerchantRatesConfigDTO> getMerchantRatesConfigById(@PathVariable Long id) {
        return merchantRatesConfigFeignClient.getMerchantRatesConfigById(id);
    }

    /**
     * 更新 商户费率设置
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateMerchantRatesConfig/{id}")
    @SysLog(title = "商户费率设置管理控制器", content = "更新")
    @ApiOperation(value = "更新商户费率设置")
    public RestResult updateMerchantRatesConfig(@PathVariable Long id, @RequestBody @ApiParam @Valid MerchantRatesConfigReq req) {
        return merchantRatesConfigFeignClient.updateMerchantRatesConfig(id, req);
    }

    /**
     * 删除 商户费率设置
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "商户费率设置管理控制器", content = "删除")
    @ApiOperation(value = "删除商户费率设置")
    public RestResult deleteMerchantRatesConfig(@PathVariable Long id) {
        return merchantRatesConfigFeignClient.deleteMerchantRatesConfig(id);
    }


    /**
     * 分页查询商户费率设置
     */
    @PostMapping("/merchantRatesConfigListPage")
    @ApiOperation(value = "分页获取 商户费率设置列表 默认获取第一页 20条记录")
    public RestResult<List<MerchantRatesConfigDTO>> merchantRatesConfigListPage(@RequestBody(required = false) @ApiParam @Valid MerchantRatesConfigPageReq pageRequest) {
        return merchantRatesConfigFeignClient.merchantRatesConfigListPage(pageRequest);
    }
}
