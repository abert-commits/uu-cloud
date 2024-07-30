package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberBlackDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.manager.api.MemberBlackClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 会员黑名单 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/memberBlack", "/memberBlack"})
@Api(description = "会员黑名单前端控制器")
public class MemberBlackController {

    private final MemberBlackClient memberBlackClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "查询会员黑名单列表")
    public RestResult<List<MemberBlackDTO>> listPage(@RequestBody @ApiParam MemberBlackReq req) {
        RestResult<List<MemberBlackDTO>> result = memberBlackClient.listPage(req);
        return result;
    }


    @PostMapping("/removeBlack")
    @ApiOperation(value = "移除黑名单")
    public RestResult removeBlack(@RequestBody @ApiParam MemberBlackReq req) {
        return  memberBlackClient.removeBlack(req);
    }

}
