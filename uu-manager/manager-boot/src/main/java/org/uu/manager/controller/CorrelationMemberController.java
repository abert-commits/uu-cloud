package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CorrelationMemberDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.manager.api.CorrelationMemberClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 关联会员信息 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-30
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/correlationMember", "/correlationMember"})
@Api(description = "关联会员信息前端控制器")
public class CorrelationMemberController {


    private final CorrelationMemberClient correlationMemberClient;


    @PostMapping("/listPage")
    @ApiOperation(value = "查询关联会员信息列表")
    public RestResult<List<CorrelationMemberDTO>> listPage(@RequestBody @ApiParam MemberBlackReq req) {
        RestResult<List<CorrelationMemberDTO>> result  = correlationMemberClient.listPage(req);
        return result;
    }

}
