package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.FrontPageConfigInfoDTO;
import org.uu.common.pay.req.InsertFrontPageConfigReq;
import org.uu.common.pay.req.QueryFrontPageConfigReq;
import org.uu.common.pay.req.UpdateFrontPageConfigReq;
import org.uu.wallet.service.FrontPageConfigService;

import java.util.List;

@RestController
@RequestMapping(value = "frontPageConfig")
@RequiredArgsConstructor
@Api(description = "首页弹窗内容控制器")
public class FrontPageConfigController {
    private final FrontPageConfigService frontPageConfigService;

    @ApiOperation("新增首页弹窗内容")
    @PostMapping(value = "/insertFrontPageConfig")
    public RestResult<Boolean> insertFrontPageConfig(@RequestBody @Validated InsertFrontPageConfigReq requestVO) {
        return this.frontPageConfigService.insertFrontPageConfig(requestVO);
    }

    @ApiOperation("删除首页弹窗内容")
    @DeleteMapping(value = "/insertFrontPageConfig/{id}")
    public RestResult<Boolean> removeFrontPageConfigById(@PathVariable Long id) {
        return this.frontPageConfigService.removeFrontPageConfigById(id);
    }

    @ApiOperation("修改首页弹窗内容")
    @PostMapping(value = "/updateFrontPageConfig")
    public RestResult<Boolean> updateFrontPageConfig(@RequestBody @Validated UpdateFrontPageConfigReq requestVO) {
        return this.frontPageConfigService.updateFrontPageConfig(requestVO);
    }

    @ApiOperation("通过ID查询首页弹窗内容")
    @GetMapping(value = "/queryFrontPageConfigById")
    public RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigById(Long id) {
        return this.frontPageConfigService.queryFrontPageConfigById(id);
    }

    @ApiOperation("通过Lang(语种)查询首页弹窗内容")
    @GetMapping(value = "/queryFrontPageConfigByLang")
    public RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigByLang(Integer lang) {
        return this.frontPageConfigService.queryFrontPageConfigByLang(lang);
    }

    @ApiOperation("分页查询首页弹窗内容")
    @PostMapping(value = "/queryFrontPageConfigPage")
    public RestResult<PageReturn<FrontPageConfigInfoDTO>> queryFrontPageConfigPage(@RequestBody QueryFrontPageConfigReq requestVO) {
        return this.frontPageConfigService.queryFrontPageConfigPage(requestVO);
    }

    @ApiOperation("首页弹窗内容列表")
    @PostMapping(value = "/frontPageConfigList")
    public RestResult<List<FrontPageConfigInfoDTO>> frontPageConfigList() {
        return this.frontPageConfigService.frontPageConfigList();
    }
}
