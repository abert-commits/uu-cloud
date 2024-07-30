package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberManualLogDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.wallet.service.IMemberManualLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 会员手动操作记录 前端控制器
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@RestController
@RequestMapping(value = {"/api/v1/memberManualLog", "/memberManualLog"})
@RequiredArgsConstructor
@Api(description = "会员手动操作记录控制器")
@ApiIgnore
public class MemberManualLogController {

    private final IMemberManualLogService memberManualLogService;

    @PostMapping("/listPage")
    @ApiOperation(value = "会员手动操作记录列表")
    public RestResult<List<MemberManualLogDTO>> listPage(@RequestBody @ApiParam MemberManualLogsReq req) {
        PageReturn<MemberManualLogDTO> payConfigPage = memberManualLogService.listPage(req);
        return RestResult.page(payConfigPage);
    }

}
