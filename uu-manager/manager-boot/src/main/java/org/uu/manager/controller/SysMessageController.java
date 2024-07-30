package org.uu.manager.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.manager.entity.SysMessage;
import org.uu.manager.req.SysMessageIdReq;
import org.uu.manager.req.SysMessageReq;
import org.uu.manager.req.SysMessageSendReq;
import org.uu.manager.service.ISysMessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2024-05-06
 */
@RestController
@RequestMapping(value = {"/api/v1/sysMessage", "/sysMessage"})
public class SysMessageController {

    @Resource
    private ISysMessageService sysMessageService;

    @PostMapping("/listPage")
    @ApiOperation(value = "系统消息列表")
    public RestResult<List<SysMessage>> listPage(@RequestBody @ApiParam SysMessageReq req) {
        PageReturn<SysMessage> sysLogPage = sysMessageService.listPage(req);
        return RestResult.page(sysLogPage);
    }

    @PostMapping("/deleted")
    @ApiOperation(value = "删除消息")
    public RestResult deleted(@RequestBody @ApiParam SysMessageIdReq req) {
        return sysMessageService.deleted(req);
    }

    @PostMapping("/read")
    @ApiOperation(value = "读取消息")
    public RestResult read(@RequestBody @ApiParam SysMessageIdReq req) {
        return sysMessageService.read(req);
    }


    @PostMapping("/sendMessage")
    @ApiOperation(value = "发送消息")
    public RestResult sendMessage(@RequestBody @ApiParam SysMessageSendReq req) {
        return sysMessageService.sendMessage(req);
    }

}
