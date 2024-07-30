package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.manager.entity.SysLog;
import org.uu.manager.req.SysLogReq;
import org.uu.manager.req.UserListPageReq;
import org.uu.manager.service.ISysLogService;
import org.uu.manager.vo.SysUserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author 
*/
    @RestController
    @Slf4j
    @RequiredArgsConstructor
    @RequestMapping("/syslog")
    @Api(description = "日志控制器")
    public class SysLogController {
    private final   ISysLogService sysLogService;

    @PostMapping("/listPage")
    @ApiOperation(value = "日志分页列表")
    public RestResult<List<SysLog>> listPage(@RequestBody SysLogReq req) {

        PageReturn<SysLog> sysLogPage = sysLogService.listPage(req);
        return RestResult.page(sysLogPage);
    }


}
