package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.common.pay.req.MemberLoginLogsReq;
import org.uu.manager.api.UserTextMessageFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 会员登录日志表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-01-13
 */
@RequestMapping("/api/v1/memberLoginLogs")
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "会员登录日志控制器")
public class MemberLoginLogsController {

    private final UserTextMessageFeignClient userTextMessageFeignClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "获取会员操作日志列表")
    public RestResult<List<MemberLoginLogsDTO>> listPage(@RequestBody @ApiParam MemberLoginLogsReq req) {
        RestResult<List<MemberLoginLogsDTO>> payConfigPage = userTextMessageFeignClient.memberLoginLogsListPage(req);
        return payConfigPage;
    }

}
