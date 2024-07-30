package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.dto.MemberManualLogDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.wallet.service.IMemberLevelConfigService;
import org.uu.wallet.service.IMemberManualLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 会员等级配置 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@RequestMapping(value = {"/api/v1/memberLevelConfig", "/memberLevelConfig"})
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "会员等级配置控制器")
@ApiIgnore
public class MemberLevelConfigController {

    private final IMemberLevelConfigService memberLevelConfigService;

    @PostMapping("/listPage")
    @ApiOperation(value = "查询会员等级配置列表")
        public RestResult<List<MemberLevelConfigDTO>> listPage(@RequestBody @ApiParam MemberManualLogsReq req) {
        PageReturn<MemberLevelConfigDTO> payConfigPage = memberLevelConfigService.listPage(req);
        return RestResult.page(payConfigPage);
    }


    @PostMapping("/update")
    @ApiOperation(value = "修改会员等级")
    public RestResult update(@RequestBody @ApiParam @Valid MemberLevelConfigDTO req) {
        RestResult result = memberLevelConfigService.updateInfo(req);
        return result;
    }

}
