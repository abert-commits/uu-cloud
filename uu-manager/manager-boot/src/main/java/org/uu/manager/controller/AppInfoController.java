package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppInfoDTO;
import org.uu.common.pay.req.AppInfoPageReq;
import org.uu.common.pay.req.AppInfoReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.AppInfoClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author afei
 */
@RestController
@RequiredArgsConstructor
@Api(description = "app信息维护记录")
@RequestMapping(value = {"/api/v1/appInfo", "/appInfo"})
@Slf4j
public class AppInfoController {
    @Resource
    private AppInfoClient appInfoClient;


    @PostMapping("/appInfoPage")
    @ApiOperation(value = "app信息维护分页列表")
    public RestResult<List<AppInfoDTO>> appInfoPage(@RequestBody @ApiParam AppInfoPageReq req) {
        return appInfoClient.appInfoPage(req);
    }


    @PostMapping("/addAppInfo")
    @SysLog(title = "app信息管理控制器", content = "新增")
    @ApiOperation(value = "新增app信息")
    public RestResult addAppInfo(@RequestBody @ApiParam @Valid AppInfoReq req) {
        return appInfoClient.addAppInfo(req);
    }


    @PostMapping("/updateAppInfo/{id}")
    @SysLog(title = "app信息", content = "更新")
    @ApiOperation(value = "更新app信息")
    public RestResult updateAppInfo(@PathVariable Long id, @RequestBody @ApiParam @Valid AppInfoReq req) {
        return appInfoClient.updateAppInfo(id, req);
    }

}
