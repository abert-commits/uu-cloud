package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.pay.dto.MemberAuthListDTO;
import org.uu.common.pay.dto.MemberGroupDTO;
import org.uu.common.pay.dto.MemberGroupListPageDTO;
import org.uu.common.pay.req.MemberGroupAddReq;
import org.uu.common.pay.req.MemberGroupIdReq;
import org.uu.common.pay.req.MemberGroupListPageReq;
import org.uu.common.pay.req.MemberGroupReq;
import org.uu.wallet.Enum.MemberAuthListEnum;
import org.uu.wallet.entity.MemberGroup;
import org.uu.wallet.service.IMemberGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

    import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.List;

/**
* @author 
*/
    @RestController
    @Slf4j
    @RequiredArgsConstructor
    @RequestMapping(value = {"/api/v1/memberGroup", "/memberGroup"})
    @ApiIgnore
    public class MemberGroupController {
        private final IMemberGroupService  memberGroupService;


    @PostMapping("/listpage")
    @ApiOperation(value = "会员分组列表")
    public RestResult<List<MemberGroupListPageDTO>> list(@RequestBody @ApiParam MemberGroupListPageReq req) {
        log.info("{}会员分组列表接口","ar-wallet");
        PageReturn<MemberGroupListPageDTO> payConfigPage = memberGroupService.listPage(req);
        return RestResult.page(payConfigPage);
    }


    @PostMapping("/create")
    @ApiOperation(value = "新建会员组")
    public RestResult<MemberGroupDTO> save(@RequestBody @ApiParam MemberGroupAddReq req) {
        MemberGroup memberGroup = new MemberGroup();
        BeanUtils.copyProperties(req,memberGroup);
        if(req.getAuthList()!=null&&req.getAuthList().size()>0){
            String s = String.join(",",req.getAuthList());
            memberGroup.setAuthList(s);
        }
        memberGroupService.save(memberGroup);
        MemberGroupDTO memberGroupDTO = new MemberGroupDTO();
        BeanUtils.copyProperties(memberGroup,memberGroupDTO);

        return RestResult.ok(memberGroupDTO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改会员组")
    public RestResult<MemberGroupDTO> update(@RequestBody @ApiParam MemberGroupReq req) {
        MemberGroup memberGroup = new MemberGroup();
        BeanUtils.copyProperties(req,memberGroup);
        if(req.getAuthList()!=null&&req.getAuthList().size()>0){
            String s = String.join(",",req.getAuthList());
            memberGroup.setAuthList(s);
        }
        memberGroupService.updateById(memberGroup);
        MemberGroupDTO memberGroupDTO = new MemberGroupDTO();
        if(!StringUtils.isEmpty(memberGroup.getAuthList())){
            String[] s = memberGroup.getAuthList().split(",");
            List<String> list = Arrays.asList(s);
            memberGroupDTO.setAuthList(list);
        }
        BeanUtils.copyProperties(memberGroup,memberGroupDTO);
        return RestResult.ok(memberGroupDTO);
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "修改会员组")
    public RestResult<MemberGroupDTO> getInfo(@RequestBody @ApiParam MemberGroupIdReq req) {
        MemberGroup memberGroup = new MemberGroup();
        BeanUtils.copyProperties(req,memberGroup);
        MemberGroup rmemberGroup = memberGroupService.getById(memberGroup);
        MemberGroupDTO memberGroupDTO = new MemberGroupDTO();
        if(!StringUtils.isEmpty(rmemberGroup.getAuthList())){
           String[] s = rmemberGroup.getAuthList().split(",");
          List<String> list = Arrays.asList(s);
            memberGroupDTO.setAuthList(list);
        }

        BeanUtils.copyProperties(rmemberGroup,memberGroupDTO);
        return RestResult.ok(memberGroupDTO);
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除会员组")
    public RestResult delete(@RequestBody @ApiParam MemberGroupIdReq req) {
        MemberGroup memberGroup = new MemberGroup();
        BeanUtils.copyProperties(req,memberGroup);
        memberGroupService.removeById(memberGroup);
        MemberGroupDTO memberGroupDTO = new MemberGroupDTO();
        BeanUtils.copyProperties(memberGroup,memberGroupDTO);
        return RestResult.ok();
    }

    @PostMapping("/authList")
    @ApiOperation(value = "权限列表")
    public RestResult<List<MemberAuthListDTO>> authList() {
       List<MemberAuthListDTO> list = MemberAuthListEnum.getList();
        return RestResult.ok(list);
    }



    }
