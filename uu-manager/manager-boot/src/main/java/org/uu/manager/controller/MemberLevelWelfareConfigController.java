package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.dto.MemberLevelWelfareConfigDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MemberLevelConfigClient;
import org.uu.manager.api.MemberLevelWelfareConfigClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 会员等级福利配置 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-10
 */
@RequestMapping(value = {"/api/v1/memberLevelWelfareConfig", "/memberLevelWelfareConfig"})
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "会员等级福利配置控制器")
public class MemberLevelWelfareConfigController {


    private final MemberLevelWelfareConfigClient memberLevelWelfareConfigClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "会员等级福利配置列表")
    public RestResult<List<MemberLevelWelfareConfigDTO>> listPage(@RequestBody @ApiParam MemberManualLogsReq req) {
        RestResult<List<MemberLevelWelfareConfigDTO>> result = memberLevelWelfareConfigClient.listPage(req);
        return result;
    }


    @PostMapping("/update")
    @ApiOperation(value = "修改会员等级福利配置")
    @SysLog(title="会员等级福利配置控制器",content = "修改会员等级福利配置")
    public RestResult update(@RequestBody @ApiParam @Valid MemberLevelWelfareConfigDTO req) {
        RestResult result = memberLevelWelfareConfigClient.update(req);
        return result;
    }

}
