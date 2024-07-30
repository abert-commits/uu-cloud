package org.uu.wallet.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.*;
import org.uu.wallet.service.AsyncNotifyService;
import org.uu.wallet.service.IKycCenterService;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.BankKycTransactionVo;
import org.uu.wallet.vo.KycBanksVo;
import org.uu.wallet.vo.KycBindLinkStatusVo;
import org.uu.wallet.vo.KycPartnersVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/kycCenter")
@Api(description = "KYC控制器")
public class KycCenterController {

    private final IKycCenterService kycCenterService;

    @GetMapping("/queryKycBindlinkStatus")
    @ApiOperation(value = "前台-获取当前用户是否kyc绑定及活跃链接")
    public RestResult<KycBindLinkStatusVo> queryKycBindlinkStatus() {
        return kycCenterService.queryKycBindlinkStatus();
    }

    /**
     * 获取KYC Partner列表
     *
     * @return {@link KycRestResult}<{@link List}<{@link KycPartnersVo}>>
     */
    @PostMapping("/getKycPartners")
    @ApiOperation(value = "获取KYC Partner列表")
    public KycRestResult<List<KycPartnersVo>> getKycPartners(@RequestBody @ApiParam KycPartnerListReq req) {
        return kycCenterService.getKycPartners(req);
    }

    /**
     * 获取KYC Partner列表
     *
     * @return {@link KycRestResult}<{@link List}<{@link KycPartnersVo}>>
     */
    @PostMapping("/getAvailableKycPartners")
    @ApiOperation(value = "获取可连接的KYC Partner列表")
    public KycRestResult<List<KycPartnersVo>> getAvailableKycPartners(@RequestBody @ApiParam KycPartnerListReq req) {
        return kycCenterService.getAvailableKycPartners(req);
    }


    /**
     * 添加 KYC Partner
     *
     * @param kycPartnerReq
     * @param request
     * @return {@link KycRestResult}
     */
    @PostMapping("/addKycPartner")
    @ApiOperation(value = "添加 KYC Partner")
    public KycRestResult addKycPartner(@RequestBody @ApiParam KycPartnerReq kycPartnerReq, HttpServletRequest request) {
        return kycCenterService.addKycPartner(kycPartnerReq, request);
    }

    /**
     * 校验 KYC Partner 参数信息
     *
     * @param kycPartnerReq req
     * @return {@link KycRestResult}
     */
    @PostMapping("/checkKycPartnerParams")
    @ApiOperation(value = "校验添加kyc参数")
    public KycRestResult checkKycPartnerParams(@RequestBody @ApiParam KycPartnerReq kycPartnerReq) {
        return kycCenterService.checkKycPartnerParams(kycPartnerReq);
    }


    /**
     * 连接KYC
     *
     * @param linkKycPartnerReq
     * @param request
     * @return {@link KycRestResult}
     */
    @PostMapping("/linkKycPartner")
    @ApiOperation(value = "连接KYC Partner")
    public KycRestResult linkKycPartner(@RequestBody @ApiParam LinkKycPartnerReq linkKycPartnerReq, HttpServletRequest request) {
        return kycCenterService.linkKycPartner(linkKycPartnerReq, request);
    }

    /**
     * 获取银行列表
     *
     * @return {@link KycRestResult}<{@link List}<{@link KycBanksVo}>>
     */
    @PostMapping("/getBanks")
    @ApiOperation(value = "获取 KYC银行列表")
    public KycRestResult<List<KycBanksVo>> getBanks(@RequestBody @ApiParam KycBankReq req){
        return kycCenterService.getBanks(req);
    }


    /**
     * 开始卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    @PostMapping("/startSell")
    @ApiOperation(value = "开始卖出")
    public KycRestResult startSell(@RequestBody @ApiParam KycSellReq kycSellReq, HttpServletRequest request) {
        return kycCenterService.startSell(kycSellReq, request);
    }


    /**
     * 停止卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    @PostMapping("/stopSell")
    @ApiOperation(value = "停止卖出")
    public KycRestResult stopSell(@RequestBody @ApiParam KycSellReq kycSellReq, HttpServletRequest request) {
        return kycCenterService.stopSell(kycSellReq, request);
    }

    /**
     * kyc自动完成任务
     *
     * @return {@link KycRestResult}
     */
    @GetMapping("/pullTransactionJob")
    @ApiOperation(value = "kyc自动完成任务")
    public void pullTransactionJob() {
        kycCenterService.pullTransactionJob();
    }


    /**
     * kyc自动完成任务
     *
     * @return {@link KycRestResult}
     */
    @PostMapping("/activate")
    @ApiOperation(value = "启用/禁用kyc")
    public KycRestResult activate(@RequestBody @ApiParam KycActivateReq kycActivateReq) {
        return kycCenterService.activate(kycActivateReq);
    }

//    @Resource
//    RedisUtil redisUtil;
//    @Resource
//    ArProperty arProperty;
//    /**
//     * kyc自动完成任务
//     *
//     * @return {@link KycRestResult}
//     */
//    @GetMapping("/test")
//    @ApiOperation(value = "kyc自动完成任务")
//    public void test() {
//        (buyerOrder=MR2024072504184500542, buyerMemberId=580398,
//                sellerOrder=W2024072504183100541, sellerMemberId=test7122785,
//                orderAmount=100.00, type=2, withdrawUpi=9417, currency=INR, utr=null, kycId=5)
//        KycAutoCompleteReq tt = new KycAutoCompleteReq();
//        tt.setType("2");
//        tt.setCurrency("INR");
//        tt.setBuyerOrder("MR2024072504184500542");
//        tt.setSellerOrder("W2024072504183100541");
//        tt.setBuyerMemberId("580398");
//        tt.setSellerMemberId("test7122785");
//        tt.setOrderAmount(new BigDecimal(100));
//        tt.setWithdrawUpi("9417");
//        tt.setKycId("5");
//        kycCenterService.completePayment(tt);
//    }
}


