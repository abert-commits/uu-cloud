package org.uu.manager.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "member-info")
public interface MemberInfoClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/memberInfo/createMemberInfo")
    RestResult<MemberInfolistPageDTO> createMemberInfo(@RequestBody MemberInfoReq req);


    @PostMapping("/api/v1/memberInfo/findGroupInfo")
    RestResult<PageReturn<GroupInfoDTO>> findGroupInfo(@RequestBody @Validated FindGroupInfoReq requestVO);

    /**
     *
     * @param req
     * @return
     */
    @PostMapping("/api/v1/memberInfo/listpage")
    RestResult<List<MemberInfolistPageDTO>> listpage(@RequestBody MemberInfoListPageReq req);


    /**
     * 详情
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/memberInfo/recharge")
    RestResult<MemberInfolistPageDTO> recharge(@RequestBody MemberInfoRechargeReq req);


    @PostMapping("/api/v1/memberInfo/freeze")
    RestResult<MemberInfolistPageDTO> freeze(@RequestBody MemberInfoFreezeReq req);



    @PostMapping("/api/v1/memberInfo/unfreeze")
    RestResult<MemberInfolistPageDTO> unfreeze(@RequestBody MemberInfoFreezeReq req);

    @PostMapping("/api/v1/memberInfo/bonus")
    RestResult<MemberInfolistPageDTO> bonus(@RequestBody MemberInfoBonusReq req);

    @PostMapping("/api/v1/memberInfo/resetpwd")
    RestResult<MemberInfolistPageDTO> resetpwd(@RequestBody MemberInfoIdReq req);
    @PostMapping("/api/v1/memberInfo/remark")
    RestResult<MemberInfolistPageDTO> remark(@RequestBody MemberInfoIdReq req);
    @PostMapping("/api/v1/memberInfo/getInfo")
    RestResult<MemberInfoDTO> getInfo(@RequestBody MemberInfoIdGetInfoReq req);
    @PostMapping("/api/v1/memberInfo/withdrawal")
    RestResult<MemberInfolistPageDTO> withdrawal(@RequestBody MemberInfoWithdrawalReq req);

    @PostMapping("/api/v1/memberInfo/disableBatch")
    RestResult<List<MemberInfolistPageDTO>> disableBatch(@RequestBody MemberInfoBatchIdReq req);
    @PostMapping("/api/v1/memberInfo/enableBatch")
    RestResult<List<MemberInfolistPageDTO>> enableBatch(@RequestBody MemberInfoBatchIdReq req);

    @PostMapping("/api/v1/memberInfo/disableBuyBatch")
    RestResult<List<MemberInfolistPageDTO>> disableBuyBatch(@RequestBody MemberInfoBatchIdReq req);

    @PostMapping("/api/v1/memberInfo/enableBuyBatch")
    RestResult<List<MemberInfolistPageDTO>> enableBuyBatch(@RequestBody MemberInfoBatchIdReq req);

    @PostMapping("/api/v1/memberInfo/disableSellBatch")
    RestResult<List<MemberInfolistPageDTO>> disableSellBatch(@RequestBody MemberInfoBatchIdReq req);
    @PostMapping("/api/v1/memberInfo/enableSellBatch")
    RestResult<List<MemberInfolistPageDTO>> enableSellBatch(@RequestBody MemberInfoBatchIdReq req);

    @PostMapping("/api/v1/memberInfo/buyStatusChange")
    RestResult<MemberInfolistPageDTO> buyStatusChange(@RequestBody @ApiParam MemberInfoBuyStatusReq req);


    @PostMapping("/api/v1/memberInfo/sellStatusChange")
    RestResult<MemberInfolistPageDTO> sellStatusChange(@RequestBody @ApiParam MemberInfoSellStatusReq req);


    @PostMapping("/api/v1/memberInfo/lastBuyRecord")
    RestResult<List<MemberInfoOrderDTO>> lastBuyRecord(@RequestBody @ApiParam MemberInfoIdReq req);


    @PostMapping("/api/v1/memberInfo/lastSellRecord")
    RestResult<List<MemberInfoOrderDTO>> lastSellRecord(@RequestBody @ApiParam MemberInfoIdReq req);



    @PostMapping("/api/v1/memberInfo/lastLogin")
    public RestResult<List<MemberInfoLoginDTO>> lastLogin(@RequestBody @ApiParam MemberInfoIdReq req);


    @PostMapping("/api/v1/memberInfo/realName")
    RestResult<List<MemberRealNamelistPageDTO>> realName(@RequestBody @ApiParam MemberInfoRealNameListReq req);

    @PostMapping("/api/v1/memberInfo/update")
    RestResult<MemberRealNamelistPageDTO> update(MemberInfoRealNameReq req);

    @PostMapping("/api/v1/memberInfo/resetPayPwd")
    RestResult<MemberInfolistPageDTO> resetPayPwd(@RequestBody MemberInfoIdReq req);

    @PostMapping("/api/v1/memberInfo/getMemberOrderOverview")
    RestResult<MemberOrderOverviewDTO> getMemberOrderOverview(CommonDateLimitReq commonDateLimitReq);

    @PostMapping("/api/v1/paymentOrder/getUsdtData")
    RestResult<MemberOrderOverviewDTO> getUsdtData(CommonDateLimitReq commonDateLimitReq);

    @PostMapping("/api/v1/memberInfo/merchantListPage")
    RestResult<List<MerchantMemberInfoPageDTO>> merchantListPage(MemberInfoListPageReq req);

    @PostMapping("/api/v1/memberInfo/relationMemberList")
    RestResult<List<MemberInfolistPageDTO>> relationMemberList(MemberInfoListPageReq req);

    @PostMapping("/api/v1/memberLevelChange/listPage")
    List<MemberLevelChangeDTO> memberLevelChangeList(@RequestParam(value = "memberId") String memberId);

    @PostMapping("/api/v1/memberInfo/getCreditScoreInfo")
    RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo(MemberCreditScoreInfoIdReq req);

    @PostMapping("/api/v1/memberInfo/updateCreditScore")
    RestResult<MemberInfolistPageDTO> updateCreditScore(MemberInfoCreditScoreReq req);

    @PostMapping("/api/v1/memberInfo/getLevelNum")
    List<MemberLevelInfoDTO> getLevelNum(@RequestParam(value = "merchantCode") String merchantCode);

    @PostMapping("/api/v1/memberInfo/cashBackBatch")
    RestResult<List<CashBackOrderProcessDTO>> cashBackBatch(@RequestBody MemberInfoBatchIdReq req);

    @PostMapping("/inviteLink/inviteCodeList")
    RestResult<PageReturn<InviteCodeInfoDTO>> inviteCodeList(@RequestBody InviteCodeInfoReq requestVO);
}
