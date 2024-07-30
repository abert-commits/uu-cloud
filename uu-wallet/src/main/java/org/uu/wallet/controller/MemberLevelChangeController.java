package org.uu.wallet.controller;


import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLevelChangeDTO;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.wallet.service.IMemberLevelChangeService;
import org.uu.wallet.service.IMemberLevelConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 会员等级变化记录 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-10
 */
@RequestMapping(value = {"/api/v1/memberLevelChange", "/memberLevelChange"})
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "会员等级变化记录控制器")
@ApiIgnore
public class MemberLevelChangeController {


    private final IMemberLevelChangeService memberLevelChangeService;

    @PostMapping("/listPage")
    @ApiOperation(value = "会员等级变化记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "会员ID", required = true, dataType = "String")
    })
    public List<MemberLevelChangeDTO> listPage(String memberId) {
        return memberLevelChangeService.listPage(memberId);
    }
}
