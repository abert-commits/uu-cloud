package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppInfoDTO;
import org.uu.common.pay.req.AppInfoDeviceReq;
import org.uu.common.pay.req.AppInfoPageReq;
import org.uu.common.pay.req.AppInfoReq;
import org.uu.wallet.service.IAppInfoService;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * app信息维护表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-25
 */
@RestController
@RequestMapping("/app-info")
public class AppInfoController {

    @Resource
    private IAppInfoService appInfoService;


    @PostMapping("/getAppInfo")
    @ApiOperation(value = "前台-查询app版本信息")
    public RestResult<AppInfoDTO> getAppInfo(@RequestBody @ApiParam AppInfoDeviceReq req) {
        return appInfoService.getAppInfoByDevice(req.getDevice());
    }

    @PostMapping("/appInfoPage")
    @ApiOperation(value = "app信息维护分页列表")
    public RestResult<List<AppInfoDTO>> appInfoPage(@RequestBody @ApiParam AppInfoPageReq req) {
        PageReturn<AppInfoDTO> pageReturn = appInfoService.appInfoPage(req);
        return RestResult.page(pageReturn);
    }

    /**
     * 新增 app信息
     */
    @PostMapping("/addAppInfo")
    @ApiIgnore
    public RestResult addAppInfo(@RequestBody @ApiParam @Valid AppInfoReq req) {
        return appInfoService.addAppInfo(req);
    }


    /**
     * 更新 app信息
     */
    @PostMapping("/updateAppInfo/{id}")
    @ApiIgnore
    public RestResult updateAppInfo(@PathVariable Long id, @RequestBody @ApiParam @Valid AppInfoReq req) {
        return appInfoService.updateAppInfo(id, req);
    }

}
