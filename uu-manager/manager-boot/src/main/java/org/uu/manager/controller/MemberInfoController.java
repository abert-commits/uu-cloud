package org.uu.manager.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MemberInfoClient;
import org.uu.manager.req.CommonReq;
import org.uu.manager.service.IBiPaymentOrderService;
import org.uu.manager.service.IMerchantInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/memberInfoAdmin", "/memberInfoAdmin"})
@Api(description = "会员控制器")
public class MemberInfoController {
    private final MemberInfoClient memberInfoClient;
    private final IBiPaymentOrderService iBiPaymentOrderService;


    @PostMapping("/createMemberInfo")
    @SysLog(title = "会员控制器", content = "创建会员")
    @ApiOperation(value = "创建会员")
    public RestResult<MemberInfolistPageDTO> save(@RequestBody @ApiParam MemberInfoReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.createMemberInfo(req);

        return result;
    }

    @PostMapping("/findGroupInfo")
    @SysLog(title = "会员控制器", content = "团队信息")
    @ApiOperation(value = "团队信息")
    public RestResult<PageReturn<GroupInfoDTO>> findGroupInfo(@RequestBody @ApiParam FindGroupInfoReq req) {
        return  memberInfoClient.findGroupInfo(req);
    }


    @PostMapping("/listpage")
    @ApiOperation(value = "获取会员列表")
    public RestResult<List<MemberInfolistPageDTO>> listpage(@RequestBody @ApiParam MemberInfoListPageReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.listpage(req);
        return result;
    }

    @PostMapping("/getLevelNum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantCode", value = "商户code", required = true, dataType = "String")
    })
    @ApiOperation(value = "获取会员等级人数")
    public  RestResult<List<MemberLevelInfoDTO>> getLevelNum(String merchantCode) {
        List<MemberLevelInfoDTO> result = memberInfoClient.getLevelNum(merchantCode);
        return RestResult.ok(result);
    }

    @PostMapping("/relationMemberList")
    @ApiOperation(value = "获取关联会员信息列表")
    public RestResult<List<MemberInfolistPageDTO>> relationMemberList(@RequestBody @ApiParam MemberInfoListPageReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.relationMemberList(req);
        return result;

    }

    @PostMapping("/memberLevelChangeList")
    @ApiOperation(value = "获取会员等级变化记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "会员ID", required = true, dataType = "String")
    })
    public RestResult<List<MemberLevelChangeDTO>> memberLevelChangeList(String memberId) {
        List<MemberLevelChangeDTO> result = memberInfoClient.memberLevelChangeList(memberId);
        return RestResult.ok(result);
    }

    @PostMapping("/merchantListPage")
    @ApiOperation(value = "获取商户会员列表")
    public RestResult<List<MerchantMemberInfoPageDTO>> merchantListPage(@RequestBody @ApiParam MemberInfoListPageReq req) {
        RestResult<List<MerchantMemberInfoPageDTO>> result = memberInfoClient.merchantListPage(req);
        return result;
    }

    @PostMapping("/recharge")
    @SysLog(title = "会员控制器", content = "人工上分")
    @ApiOperation(value = "人工上分")
    public RestResult<MemberInfolistPageDTO> recharge(@RequestBody @ApiParam MemberInfoRechargeReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.recharge(req);
        return result;

    }

    @PostMapping("/realName")
    @SysLog(title = "会员控制器", content = "会员实名")
    @ApiOperation(value = "会员实名")
    public RestResult<List<MemberRealNamelistPageDTO>> list(@RequestBody @ApiParam MemberInfoRealNameListReq req) {
        return memberInfoClient.realName(req);
    }


    @PostMapping("/withdrawal")
    @SysLog(title = "会员控制器", content = "人工下分")
    @ApiOperation(value = "人工下分")
    public RestResult<MemberInfolistPageDTO> withdrawal(@RequestBody @ApiParam MemberInfoWithdrawalReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.withdrawal(req);
        return result;

    }


    @PostMapping("/update")
    @SysLog(title = "会员控制器", content = "编辑")
    @ApiOperation(value = "编辑")
    public RestResult<MemberRealNamelistPageDTO> update(@RequestBody @ApiParam MemberInfoRealNameReq req) {
        RestResult<MemberRealNamelistPageDTO> result = memberInfoClient.update(req);
        return result;

    }


    @PostMapping("/freeze")
    @SysLog(title = "会员控制器", content = "冻结")
    @ApiOperation(value = "冻结")
    public RestResult<MemberInfolistPageDTO> freeze(@RequestBody @ApiParam MemberInfoFreezeReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.freeze(req);
        return result;

    }

    @PostMapping("/unfreeze")
    @SysLog(title = "会员控制器", content = "解冻")
    @ApiOperation(value = "解冻")
    public RestResult<MemberInfolistPageDTO> unfreeze(@RequestBody @ApiParam MemberInfoFreezeReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.unfreeze(req);
        return result;

    }

    @PostMapping("/bonus")
    @SysLog(title = "会员控制器", content = "奖励")
    @ApiOperation(value = "奖励")
    public RestResult<MemberInfolistPageDTO> bonus(@RequestBody @ApiParam MemberInfoBonusReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.bonus(req);
        return result;

    }

    @PostMapping("/resetpwd")
    @SysLog(title = "会员控制器", content = "重置密码")
    @ApiOperation(value = "重置密码")
    public RestResult<MemberInfolistPageDTO> resetpwd(@RequestBody @ApiParam MemberInfoIdReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.resetpwd(req);
        return result;
    }


    @PostMapping("/resetPayPwd")
    @SysLog(title = "会员控制器", content = "重置支付密码")
    @ApiOperation(value = "重置支付密码")
    public RestResult<MemberInfolistPageDTO> resetPayPwd(@RequestBody @ApiParam MemberInfoIdReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.resetPayPwd(req);
        return result;

    }


    @PostMapping("/remark")
    @SysLog(title = "会员控制器", content = "备注")
    @ApiOperation(value = "备注")
    public RestResult<MemberInfolistPageDTO> remark(@RequestBody @ApiParam MemberInfoIdReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.remark(req);
        return result;

    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "会员信息")
    public RestResult<MemberInfoDTO> getInfo(@RequestBody @ApiParam MemberInfoIdGetInfoReq req) {
        RestResult<MemberInfoDTO> result = memberInfoClient.getInfo(req);
        return result;

    }


    @PostMapping("/disableBatch")
    @ApiOperation(value = "批量禁用")
    public RestResult<List<MemberInfolistPageDTO>> disableBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.disableBatch(req);
        return result;

    }


    @PostMapping("/enableBatch")
    @ApiOperation(value = "批量启用")
    public RestResult<List<MemberInfolistPageDTO>> enableBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {

        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.enableBatch(req);
        return result;
    }


    @PostMapping("/disableBuyBatch")
    @ApiOperation(value = "批量禁用买入")
    public RestResult<List<MemberInfolistPageDTO>> disableBuyBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.disableBuyBatch(req);
        return result;
    }

    @PostMapping("/enableBuyBatch")
    @ApiOperation(value = "批量启用买入")
    public RestResult<List<MemberInfolistPageDTO>> enableBuyBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.enableBuyBatch(req);
        return result;


    }


    @PostMapping("/disableSellBatch")
    @ApiOperation(value = "批量禁用卖出")
    public RestResult<List<MemberInfolistPageDTO>> disableSellBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.disableSellBatch(req);
        return result;

    }

    @PostMapping("/enableSellBatch")
    @ApiOperation(value = "批量启用卖出")
    public RestResult<List<MemberInfolistPageDTO>> enableSellBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        RestResult<List<MemberInfolistPageDTO>> result = memberInfoClient.enableSellBatch(req);
        return result;

    }


    @PostMapping("/buyStatusChange")
    @ApiOperation(value = "买入状态")
    public RestResult<MemberInfolistPageDTO> buyStatusChange(@RequestBody @ApiParam MemberInfoBuyStatusReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.buyStatusChange(req);
        return result;
    }

    @PostMapping("/sellStatusChange")
    @ApiOperation(value = "卖出状态修改")
    public RestResult<MemberInfolistPageDTO> sellStatusChange(@RequestBody @ApiParam MemberInfoSellStatusReq req) {
        RestResult<MemberInfolistPageDTO> result = memberInfoClient.sellStatusChange(req);
        return result;

    }


    @PostMapping("/lastBuyRecord")
    @ApiOperation(value = "最后买入记录")
    public RestResult<List<MemberInfoOrderDTO>> lastBuyRecord(@RequestBody @ApiParam MemberInfoIdReq req) {
        RestResult<List<MemberInfoOrderDTO>> result = memberInfoClient.lastBuyRecord(req);
        return result;
    }


    @PostMapping("/lastSellRecord")
    @ApiOperation(value = "最后卖出记录")
    public RestResult<List<MemberInfoOrderDTO>> lastSellRecord(@RequestBody @ApiParam MemberInfoIdReq req) {
//
        RestResult<List<MemberInfoOrderDTO>> result = memberInfoClient.lastSellRecord(req);
        return result;

    }


    @PostMapping("/lastLogin")
    @ApiOperation(value = "最后登录记录")
    public RestResult<List<MemberInfoLoginDTO>> lastLogin(@RequestBody @ApiParam MemberInfoIdReq req) {
        RestResult<List<MemberInfoLoginDTO>> result = memberInfoClient.lastLogin(req);
        return result;

    }

    @PostMapping("/getMemberOrderOverview")
    @ApiOperation(value = "获取买入卖出订单统计")
    public RestResult<MemberOrderOverviewDTO> getMemberOrderOverview(@RequestBody @ApiParam CommonDateLimitReq req) {
        RestResult<MemberOrderOverviewDTO> usdtData = memberInfoClient.getUsdtData(req);
        return iBiPaymentOrderService.getMemberOrderOverview(req, usdtData);
    }

    @PostMapping("/updateCreditScore")
    @ApiOperation(value = "更新用户信用分")
    public  RestResult<MemberInfolistPageDTO> updateCreditScore(@RequestBody @ApiParam MemberInfoCreditScoreReq req) {
        return memberInfoClient.updateCreditScore(req);
    }

    @PostMapping("/getCreditScoreInfo")
    @ApiOperation(value = "获取信用分信息")
    public  RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo(@RequestBody @ApiParam MemberCreditScoreInfoIdReq req) {
        return memberInfoClient.getCreditScoreInfo(req);
    }


    @PostMapping("/cashBackBatch")
    @ApiOperation(value = "批量退回余额")
    public RestResult<List<CashBackOrderProcessDTO>> cashBackBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        return memberInfoClient.cashBackBatch(req);
    }


    @ApiOperation("根据会员ID获取邀请码列表")
    @PostMapping(value = "/inviteCodeList")
    public RestResult<PageReturn<InviteCodeInfoDTO>> inviteCodeList(@RequestBody @Validated InviteCodeInfoReq requestVO) {
        return this.memberInfoClient.inviteCodeList(requestVO);
    }
}
