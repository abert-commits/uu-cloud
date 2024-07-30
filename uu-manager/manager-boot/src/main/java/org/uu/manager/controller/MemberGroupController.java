package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberAuthListDTO;
import org.uu.common.pay.dto.MemberGroupDTO;
import org.uu.common.pay.dto.MemberGroupListPageDTO;
import org.uu.common.pay.req.MemberGroupAddReq;
import org.uu.common.pay.req.MemberGroupIdReq;
import org.uu.common.pay.req.MemberGroupListPageReq;
import org.uu.common.pay.req.MemberGroupReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MemberGroupClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
* @author
*/
    @RestController
    @Slf4j
    @RequiredArgsConstructor
    @Api(description = "会员分组控制器")
    @RequestMapping(value = {"/api/v1/memberGroupAdmin", "/memberGroupAdmin"})
    public class MemberGroupController {
        private final MemberGroupClient memberGroupClient;


    @PostMapping("/listpage")
    @ApiOperation(value = "会员分组列表")
    public RestResult<List<MemberGroupListPageDTO>> list(@RequestBody @ApiParam MemberGroupListPageReq req) {
        log.info("{}会员分组列表接口","uu-manager");
        RestResult<List<MemberGroupListPageDTO>> result = memberGroupClient.listpage(req);
        return result;
    }


    @PostMapping("/create")
    @SysLog(title="会员分组控制器",content = "新增")
    @ApiOperation(value = "新增")
    public RestResult<MemberGroupDTO> create(@RequestBody @ApiParam MemberGroupAddReq req) {
        RestResult<MemberGroupDTO> result =  memberGroupClient.create(req);

        return result;
    }

    @PostMapping("/update")
    @SysLog(title="会员分组控制器",content = "修改")
    @ApiOperation(value = "修改")
    public RestResult<MemberGroupDTO> update(@RequestBody @ApiParam MemberGroupReq req) {
        RestResult<MemberGroupDTO> result =  memberGroupClient.update(req);
        return result;
    }


    @PostMapping("/delete")
    @SysLog(title="会员分组控制器",content = "删除")
    @ApiOperation(value = "删除")
    public RestResult delete(@RequestBody @ApiParam MemberGroupIdReq req) {
        RestResult result =  memberGroupClient.delete(req);
        return result;
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "查看")
    public RestResult<MemberGroupDTO> getInfo(@RequestBody @ApiParam MemberGroupIdReq req) {
        RestResult<MemberGroupDTO> result =  memberGroupClient.getInfo(req);
        return result;
    }

    @PostMapping("/authList")
    @ApiOperation(value = "权限列表")
    public RestResult<List<MemberAuthListDTO>> authList() {
        RestResult<List<MemberAuthListDTO>> result = memberGroupClient.authList();
        return result;
    }



    }
