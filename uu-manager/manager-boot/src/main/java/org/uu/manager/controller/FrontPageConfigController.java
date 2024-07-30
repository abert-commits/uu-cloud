package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.FrontPageConfigInfoDTO;
import org.uu.common.pay.req.UpdateFrontPageConfigReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.FrontPageConfigFeignClient;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 首页弹窗内容 前端控制器
 * </p>
 *
 * @author
 * @since 2024-04-27
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "首页弹窗内容控制器")
@RequestMapping(value = {"/api/v1/frontPageConfig", "/frontPageConfig"})
public class FrontPageConfigController {

    @Resource
    FrontPageConfigFeignClient frontPageConfigFeignClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "查询首页弹窗内容")
    public RestResult<List<FrontPageConfigInfoDTO>> listPage() {
        return frontPageConfigFeignClient.frontPageConfigList();
    }


    @PostMapping("/update")
    @SysLog(title = "首页弹窗内容控制器", content = "修改首页弹窗内容")
    @ApiOperation(value = "修改首页弹窗内容")
    public RestResult update(@RequestBody @ApiParam @Valid UpdateFrontPageConfigReq req) {
        return frontPageConfigFeignClient.updateFrontPageConfig(req);
    }

}
