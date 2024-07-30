package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberOperationLogsDTO;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.common.pay.req.MemberOperationLogsReq;
import org.uu.common.pay.req.UserTextMessageReq;
import org.uu.wallet.service.IMemberOperationLogsService;
import org.uu.wallet.service.IUserVerificationCodesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 会员操作日志表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-01-13
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/memberOperationLogs", "/memberOperationLogs"})
@Api(description = "会员操作日志控制器")
@ApiIgnore
public class MemberOperationLogsController {

    private final IMemberOperationLogsService memberOperationLogsService;

    @PostMapping("/listPage")
    @ApiOperation(value = "获取用户验证码列表")
    public RestResult<List<MemberOperationLogsDTO>> listPage(@RequestBody @ApiParam MemberOperationLogsReq memberOperationLogsReq) {
        PageReturn<MemberOperationLogsDTO> payConfigPage = memberOperationLogsService.listPage(memberOperationLogsReq);
        return RestResult.page(payConfigPage);
    }
}
