package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.InviteCodeInfoDTO;
import org.uu.common.pay.req.InviteCodeInfoReq;
import org.uu.common.pay.vo.request.InviteLinkSaveRequestVO;
import org.uu.common.pay.vo.response.InviteInfoDetailResponseVO;
import org.uu.common.pay.vo.response.InviteInfoResponseVO;
import org.uu.wallet.service.InviteLinkService;

/**
 * <p>
 *  邀请链接 前端控制器
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@RestController
@RequiredArgsConstructor
@Api(description = "邀请链接控制器")
@RequestMapping(value = "/inviteLink")
public class InviteLinkController {
    private final InviteLinkService inviteLinkService;

    @ApiOperation("根据会员ID获取邀请码列表")
    @PostMapping(value = "/inviteCodeList")
    public RestResult<PageReturn<InviteCodeInfoDTO>> inviteCodeList(@RequestBody @Validated InviteCodeInfoReq requestVO) {
        return this.inviteLinkService.inviteCodeList(requestVO);
    }

    @ApiOperation("获取邀请链接列表")
    @PostMapping(value = "/inviteLinkList")
    public RestResult<PageReturn<InviteInfoDetailResponseVO>> inviteLinkList(PageRequestHome pageRequest) {
        return this.inviteLinkService.inviteLinkList(pageRequest);
    }

    @ApiOperation("删除邀请链接(默认邀请链接不允许删除)")
    @PostMapping(value = "/removeInviteLink/{id}")
    @ApiImplicitParam(name = "id", required = true, value = "邀请链接ID")
    public RestResult<Void> removeInviteLink(@PathVariable Long id) {
        return this.inviteLinkService.removeInviteLink(id);
    }

    @ApiOperation("设置默认邀请链接")
    @PostMapping(value = "/setDefaultInviteLink/{id}")
    @ApiImplicitParam(name = "id", required = true, value = "邀请链接ID")
    public RestResult<Void> setDefaultInviteLink(@PathVariable Long id) {
        return this.inviteLinkService.setDefaultInviteLink(id);
    }

    @ApiOperation("添加邀请链接(第一个自动置为默认邀请链接)")
    @PostMapping(value = "/addInviteLink")
    public RestResult<Void> addInviteLink(@RequestBody @Validated InviteLinkSaveRequestVO requestVO) {
        return this.inviteLinkService.saveInviteLink(requestVO);
    }
}
