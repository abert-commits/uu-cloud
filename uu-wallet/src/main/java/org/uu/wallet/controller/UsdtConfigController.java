package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtConfigDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.UsdtConfig;
import org.uu.wallet.service.IUsdtConfigService;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/usdtConfig", "/usdtConfig"})
@ApiIgnore
public class UsdtConfigController {
    private final IUsdtConfigService usdtConfigService;

    @PostMapping("/listpage")
    @ApiOperation(value = "下发申请表")
    public RestResult<UsdtConfigDTO> list(@RequestBody @ApiParam UsdtConfigPageReq req) {
        PageReturn<UsdtConfigDTO> payConfigPage = usdtConfigService.listPage(req);
        return RestResult.page(payConfigPage);
    }


    @PostMapping("/create")
    @ApiOperation(value = "新增usdt配置")
    public RestResult<UsdtConfigDTO> create(@RequestBody @ApiParam UsdtConfigCreateReq req) {
        UsdtConfig usdtConfig = new UsdtConfig();
        BeanUtils.copyProperties(req, usdtConfig);
        usdtConfigService.save(usdtConfig);
        UsdtConfigDTO usdtConfigDTO = new UsdtConfigDTO();
        BeanUtils.copyProperties(usdtConfig, usdtConfigDTO);
        return RestResult.ok(usdtConfigDTO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改usdt配置")
    public RestResult<UsdtConfigDTO> update(@RequestBody @ApiParam UsdtConfigReq req) {
        UsdtConfig usdtConfig = new UsdtConfig();
        BeanUtils.copyProperties(req, usdtConfig);
        usdtConfigService.updateById(usdtConfig);
        UsdtConfigDTO usdtConfigDTO = new UsdtConfigDTO();
        BeanUtils.copyProperties(usdtConfig, usdtConfigDTO);
        return RestResult.ok(usdtConfigDTO);
    }

    @PostMapping("/changeStatus")
    @ApiOperation(value = "修改状态")
    public RestResult<UsdtConfigDTO> changeStatus(@RequestBody @ApiParam UsdtConfigQueryReq req) {
        UsdtConfig usdtConfig = new UsdtConfig();
        BeanUtils.copyProperties(req, usdtConfig);
        UsdtConfig usdtConfigvo = usdtConfigService.getById(usdtConfig);
        usdtConfigvo.setStatus(req.getStatus());
        usdtConfigService.updateById(usdtConfig);
        UsdtConfigDTO usdtConfigDTO = new UsdtConfigDTO();
        BeanUtils.copyProperties(usdtConfig, usdtConfigDTO);
        return RestResult.ok(usdtConfigDTO);
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "获取配置详情")
    public RestResult<UsdtConfigDTO> getInfo(@RequestBody @ApiParam UsdtConfigIdReq req) {
        UsdtConfig usdtConfig = new UsdtConfig();
        BeanUtils.copyProperties(req, usdtConfig);
        usdtConfig = usdtConfigService.getById(usdtConfig);
        UsdtConfigDTO usdtConfigDTO = new UsdtConfigDTO();
        BeanUtils.copyProperties(usdtConfig, usdtConfigDTO);
        return RestResult.ok(usdtConfigDTO);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除")
    public RestResult delete(@RequestBody @ApiParam UsdtConfigIdReq req) {
        UsdtConfig usdtConfig = new UsdtConfig();
        BeanUtils.copyProperties(req, usdtConfig);
        usdtConfigService.removeById(usdtConfig);
//            UsdtConfigDTO usdtConfigDTO = new UsdtConfigDTO();
//            BeanUtils.copyProperties(usdtConfig,usdtConfigDTO);
        return RestResult.ok("删除成功");
    }

}
