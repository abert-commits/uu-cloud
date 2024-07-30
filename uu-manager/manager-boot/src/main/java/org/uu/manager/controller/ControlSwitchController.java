package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ControlSwitchDTO;
import org.uu.common.pay.req.*;
import org.uu.manager.api.ControlSwitchClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * <p>
 * 后台控制开关表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/controlSwitchAdmin", "/controlSwitchAdmin"})
@Api(description = "后台控制开关控制器")
public class ControlSwitchController {
    private final ControlSwitchClient controlSwitchClient;

    @PostMapping("/createControlSwitch")
    @ApiOperation(value = "创建控制开关")
    public RestResult<ControlSwitchDTO> createControlSwitch(@RequestBody @ApiParam ControlSwitchReq req) {
        return controlSwitchClient.createControlSwitch(req);
    }

    @PostMapping("/updateControlSwitchInfo")
    @ApiOperation(value = "更新开关信息")
    public RestResult<ControlSwitchDTO> updateControlSwitchInfo(@RequestBody @ApiParam ControlSwitchUpdateReq req) {
        return controlSwitchClient.updateControlSwitchInfo(req);
    }

    @PostMapping("/updateControlSwitchStatus")
    @ApiOperation(value = "开启关闭开关")
    public RestResult<ControlSwitchDTO> updateControlSwitchStatus(@RequestBody @ApiParam ControlSwitchStatusReq req) {
        return controlSwitchClient.updateControlSwitchStatus(req);
    }

    @PostMapping("/detail")
    @ApiOperation(value = "开关详情")
    public RestResult<ControlSwitchDTO> detail(@RequestBody @ApiParam ControlSwitchIdReq req) {
        return controlSwitchClient.detail(req);
    }

    @PostMapping("/listPage")
    @ApiOperation(value = "开关列表")
    public RestResult controlSwitchList(@RequestBody @ApiParam ControlSwitchPageReq req) {
        return controlSwitchClient.listPage(req);
    }

}
