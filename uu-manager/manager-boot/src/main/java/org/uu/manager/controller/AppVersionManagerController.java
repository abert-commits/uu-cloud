package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.manager.annotation.SysLog;
import org.uu.common.pay.dto.AppVersionDTO;
import org.uu.manager.service.IAppVersionManagerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.uu.manager.service.IBiOverViewStatisticsDailyService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * APP版本管理 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-20
 */
@Slf4j
@RestController
@Api(description = "APP版本管理控制器")
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/appVersionManager", "/appVersionManager"})
public class AppVersionManagerController {
    @Resource
    IAppVersionManagerService iAppVersionManagerService;

    @Resource
    IBiOverViewStatisticsDailyService biOverViewStatisticsDailyService;

    @PostMapping("/listPage")
    @ApiOperation(value = "查询APP版本管理配置")
    public RestResult<List<AppVersionDTO>> listPage() {
        OrderEventReq orderEventReq = new OrderEventReq();
        orderEventReq.setParams("{\"amount\":200.00,\"commission\":4.00}");
        orderEventReq.setEventId("6");
        biOverViewStatisticsDailyService.statisticsDaily(orderEventReq);
        List<AppVersionDTO> result = iAppVersionManagerService.listPage();
        return RestResult.ok(result);
    }


    @PostMapping("/update")
    @SysLog(title="APP版本管理控制器",content = "修改APP版本管理配置")
    @ApiOperation(value = "修改APP版本管理配置")
    public RestResult update(@RequestBody @ApiParam @Valid AppVersionDTO req) {
        RestResult result = iAppVersionManagerService.updateInfo(req);
        return result;
    }

}
