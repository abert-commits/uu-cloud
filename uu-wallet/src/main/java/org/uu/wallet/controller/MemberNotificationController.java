package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.vo.request.MemberNotificationListRequestVO;
import org.uu.common.pay.vo.response.MemberNotificationResponseVO;
import org.uu.wallet.req.TradeNotificationReq;
import org.uu.wallet.service.IMemberNotificationService;

/**
 * 消息通知控制器
 * @author
 */
@RestController
@Validated
@RequiredArgsConstructor
@Api(description = "消息通知管理控制器")
@RequestMapping("/memberNotification")
public class MemberNotificationController {

    private final IMemberNotificationService memberNotificationService;

    @ApiOperation("消息通知列表")
        @PostMapping(value = "/memberNotificationList")
    public RestResult<PageReturn<MemberNotificationResponseVO>> memberNotificationList(@RequestBody MemberNotificationListRequestVO requestVO) {
        return this.memberNotificationService.memberNotificationList(requestVO);
    }

    @ApiOperation("一键已读")
    @PostMapping(value = "/allRead")
    public RestResult<Void> allRead() {
        return this.memberNotificationService.allRead();
    }

    @ApiOperation("根据ID读取消息")
    @PostMapping(value = "/read/{id}")
    public RestResult<Void> readById(@PathVariable Long id) {
        return this.memberNotificationService.readById(id);
    }

    @ApiOperation("添加消息通知")
    @PostMapping(value = "/addNotification")
    public RestResult<Boolean> addNotification(@RequestBody TradeNotificationReq requestVO) {
        return RestResult.ok(this.memberNotificationService.insertPayNotification(requestVO));
    }
}


