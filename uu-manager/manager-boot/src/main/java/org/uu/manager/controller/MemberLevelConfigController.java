package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CollectionInfoDTO;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.req.CollectionInfoReq;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MemberLevelConfigClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class MemberLevelConfigController {

    private final MemberLevelConfigClient memberLevelConfigClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "会员等级查询列表")
        public RestResult<List<MemberLevelConfigDTO>> listPage(@RequestBody @ApiParam MemberManualLogsReq req) {
        RestResult<List<MemberLevelConfigDTO>> result = memberLevelConfigClient.listPage(req);
        return result;
    }


    @PostMapping("/update")
    @SysLog(title="会员等级配置控制器",content = "修改会员等级")
    @ApiOperation(value = "修改会员等级")
    public RestResult update(@RequestBody @ApiParam @Valid MemberLevelConfigDTO req) {
        RestResult result = memberLevelConfigClient.update(req);
        return result;
    }

}
