package org.uu.manager.controller;


import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.entity.SysWhite;
import org.uu.manager.req.SysWhiteReq;
import org.uu.manager.service.ISysWhiteService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
* @author 
*/
    @RestController
    @RequiredArgsConstructor
    @Api(description = "白名单控制器")
    @RequestMapping("/syswhite")
    public class SysWhiteController {
    public final ISysWhiteService sysWhiteService;


    @PostMapping("/listPage")
    @ApiOperation(value = "白名单分页列表")
    public RestResult<List<SysWhite>> listPage(@RequestBody SysWhiteReq req) {
                PageReturn<SysWhite> sysLogPage = sysWhiteService.listPage(req);
                return RestResult.page(sysLogPage);
        }

    @PostMapping("/save")
    @SysLog(title="白名单控制器",content = "新增")
    @ApiOperation(value = "新增")
    public RestResult<?> save(@RequestBody SysWhiteReq req) {
        return sysWhiteService.saveDeduplication(req);
    }


    @PostMapping("/del")
    @SysLog(title="白名单控制器",content = "删除")
    @ApiOperation(value = "删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "行id", required = true, dataType = "String")
    })
    public RestResult del(@RequestParam(value = "id") String id) {
        boolean result = sysWhiteService.del(id);
        return result ? RestResult.ok() : RestResult.failed();
    }


    @PostMapping("/update")
    @SysLog(title="白名单控制器",content = "更新白名单")
    @ApiOperation(value = "更新白名单")
    public RestResult update(@RequestBody @ApiParam SysWhite sysWhite) {
        Long currentUserId = UserContext.getCurrentUserId();
        if(sysWhite.getId()==null) return RestResult.failed("id不能为空");
       // MerchantInfo merchantInfo = new MerchantInfo();
       // BeanUtils.copyProperties(merchantInfoReq, merchantInfo);
        boolean su = sysWhiteService.updateById(sysWhite);
        return RestResult.ok(sysWhite);
    }

    @PostMapping("/getIp")
    @SysLog(title="白名单控制器",content = "获取当前IP是否在白名单内")
    @ApiOperation(value = "获取当前IP是否在白名单内")
    @ApiIgnore
    public boolean getIp(@RequestBody Map<String, String> params) {
        String ip = params.get("ip");
        String type = params.get("type");
        return sysWhiteService.getIp(ip, type);
    }
    }
