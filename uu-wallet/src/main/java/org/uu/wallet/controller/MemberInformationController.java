package org.uu.wallet.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.AppVersionManagerDTO;
import org.uu.common.pay.dto.MemberLevelDTO;
import org.uu.common.pay.req.NewUserGuideReq;
import org.uu.common.pay.vo.request.MemberAccountChangeDetailRequestVO;
import org.uu.common.pay.vo.request.MemberAccountChangeRequestVO;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.pay.vo.response.MemberAccountChangeResponseVO;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.MemberOperationModuleEnum;
import org.uu.wallet.annotation.LogMemberOperation;
import org.uu.wallet.entity.KycPartners;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberNotification;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.vo.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/memberInformation")
@Api(description = "前台-我的控制器")
@Validated
public class MemberInformationController {

    private final IMemberInfoService memberInfoService;
    private final IMemberNotificationService memberNotificationService;
    private final ICollectionInfoService collectionInfoService;
    private final IAppealOrderService appealOrderService;
    private final IMemberAccountChangeService memberAccountChangeService;
    private final IBuyService buyService;
    private final ISellService sellService;
    private final IBankInfoService bankInfoService;


    @GetMapping("/getCurrentMemberInfo")
    @ApiOperation(value = "前台-获取当前会员信息")
    public RestResult<MemberInformationVo> getCurrentMemberInfo() {
        //获取当前会员信息
        return memberInfoService.getCurrentMemberInfo();
    }

    @ApiOperation(value = "前台-奖励列表")
    @PostMapping(value = "/commissionRewardPage")
    public RestResult<PageReturn<MemberAccountChangeResponseVO>> commissionRewardPage(@RequestBody PageRequestHome requestVO) {
        return this.memberAccountChangeService.commissionRewardPage(requestVO);
    }

    @PostMapping("/getMemberAccountChangeInfo")
    @ApiOperation(value = "前台-获取当前会员帐变记录信息")
    public RestResult<PageReturn<MemberAccountChangeResponseVO>> getMemberAccountChangeInfo(@RequestBody @Validated MemberAccountChangeRequestVO requestVO) {
        //获取当前会员信息
        return memberInfoService.getMemberAccountChangeInfo(requestVO);
    }

    @PostMapping("/getMemberAccountChangeDetail")
    @ApiOperation(value = "前台-获取当前会员帐变记录详情")
    public RestResult<MemberAccountChangeDetailResponseVO> getMemberAccountChangeDetail(@RequestBody @Validated MemberAccountChangeDetailRequestVO requestVO) {
        return memberInfoService.getMemberAccountChangeDetail(requestVO);
    }

    @PostMapping("/idenAuthentication")
    @ApiOperation(value = "前台-实名认证")
    @LogMemberOperation(value = MemberOperationModuleEnum.REAL_NAME_AUTHENTICATION)
    public RestResult idenAuthentication(
            @NotNull(message = "ID number cannot be empty")
            @Pattern(regexp = "^[a-zA-Z0-9]{1,30}$", message = "The ID number format is incorrect")
            @ApiParam(value = "证件号 (格式为印度人证件号格式 示例: 123456789012)", required = true) @RequestParam("idCardNumber") @Valid String idCardNumber,

            @NotNull(message = "Do not leave blank for real name")
            @Pattern(regexp = "^[a-zA-Z]+(?:[\\s.][a-zA-Z]+)*$", message = "Real name format is incorrect")
            @ApiParam(value = "真实姓名 (格式为印度人真实姓名格式 示例: Priya)", required = true) @RequestParam("realName") @Valid String realName,

            @ApiParam(value = "证件照片文件", required = true) @RequestPart("idCardImage") MultipartFile idCardImage,

            @ApiParam(value = "人脸照片", required = true) @RequestPart("facePhoto") MultipartFile facePhoto
    ) {

        //实名认证处理
        return memberInfoService.idenAuthenticationProcess(idCardNumber, realName, idCardImage, facePhoto);
    }

    @PostMapping("/verifySmsCode")
    @ApiOperation(value = "前台-校验短信验证码(更换手机号使用)")
    @LogMemberOperation(value = MemberOperationModuleEnum.VERIFY_SMS_CODE)
    public RestResult verifySmsCode(@RequestBody @ApiParam @Valid VerifySmsCodeReq verifySmsCodeReq) {
        return memberNotificationService.validateSmsCode(verifySmsCodeReq) ? RestResult.ok() : RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
    }

    @PostMapping("/updatePhoneNumber")
    @ApiOperation(value = "前台-更换手机号码")
    @LogMemberOperation(value = MemberOperationModuleEnum.CHANGE_PHONE_NUMBER)
    public RestResult updatePhoneNumber(@RequestBody @ApiParam @Valid VerifySmsCodeReq verifySmsCodeReq) {
        //更换手机号码 处理
        return memberInfoService.updatePhoneNumberProcess(verifySmsCodeReq);
    }

    @PostMapping("/updateEmail")
    @ApiOperation(value = "前台-更换邮箱号")
    @LogMemberOperation(value = MemberOperationModuleEnum.CHANGE_EMAIL)
    public RestResult updateEmail(@RequestBody @ApiParam @Valid BindEmailReq bindEmailReq) {
        //更换邮箱号处理
        return memberInfoService.updateEmailProcess(bindEmailReq);
    }

    @PostMapping("/getCurrentCollectionInfo")
    @ApiOperation(value = "前台-获取当前用户UPI收款信息")
    public RestResult<PageReturn<CollectionInfoVo>> currentCollectionInfo(@RequestBody(required = false) @ApiParam @Valid PageRequestHome pageRequestHome) {
        return collectionInfoService.currentCollectionInfo(pageRequestHome);
    }

    @PostMapping("/getCurrentUserBankInfoList")
    @ApiOperation(value = "前台-获取当前用户银行卡收款信息列表")
    public RestResult<PageReturn<BankCardCollectionInfoVo>> getCurrentUserBankInfoList(@RequestBody(required = false) @ApiParam @Valid PageRequestHome pageRequestHome) {
        return collectionInfoService.getCurrentUserBankInfoList(pageRequestHome);
    }

    @PostMapping("/createcollectionInfo")
    @ApiOperation(value = "前台-添加收款信息")
    @LogMemberOperation(value = MemberOperationModuleEnum.ADD_PAYMENT_INFO)
    public RestResult createcollectionInfo(@RequestBody @ApiParam @Valid FrontendCollectionInfoReq frontendCollectionInfoReq) {
        //添加收款信息处理
        return collectionInfoService.createcollectionInfoProcessing(frontendCollectionInfoReq);
    }

    @PostMapping("/enableCollection")
    @ApiOperation(value = "前台-开启收款")
    @LogMemberOperation(value = MemberOperationModuleEnum.START_RECEIVING)
    public RestResult enableCollection(@RequestBody @ApiParam @Valid CollectioninfoIdReq collectioninfoIdReq) {
        //开启收款处理
        return collectionInfoService.enableCollectionProcessing(collectioninfoIdReq);
    }

//    @GetMapping("/stopCollection")
//    @ApiOperation(value = "前台-停止收款")
//    @LogMemberOperation(value = MemberOperationModuleEnum.STOP_RECEIVING)
//    public RestResult stopCollection(@RequestBody @ApiParam @Valid CollectioninfoIdReq collectioninfoIdReq) {
//        //停止收款处理
//        return collectionInfoService.stopCollectionProcessing(collectioninfoIdReq);
//    }

    @DeleteMapping("/deleteCollectionInfo")
    @ApiOperation(value = "前台-删除收款信息")
    @LogMemberOperation(value = MemberOperationModuleEnum.DELETE_PAYMENT_INFO)
    public RestResult deleteCollectionInfo(@ApiParam(value = "收款信息id", required = true) @NotNull(message = "Payment information id cannot be empty") @Min(value = 0, message = "The payment information id format is incorrect") @RequestParam("collectionInfoId") @Valid Long collectionInfoId) {
        //删除收款信息处理
        return collectionInfoService.deleteCollectionInfoProcessing(collectionInfoId);
    }

    @PostMapping("/updateAvatar")
    @ApiOperation(value = "前台-设置头像")
    @LogMemberOperation(value = MemberOperationModuleEnum.SET_AVATAR)
    public RestResult updateAvatar(@RequestBody @ApiParam @Valid UpdateAvatarReq updateAvatarReq) {
        //设置头像
        return memberInfoService.updateAvatar(updateAvatarReq);
    }

    @PostMapping("/updateNickname")
    @ApiOperation(value = "前台-设置昵称")
    @LogMemberOperation(value = MemberOperationModuleEnum.SET_NICKNAME)
    public RestResult updateNickname(@RequestBody @ApiParam @Valid UpdateNicknameReq updateNicknameReq) {
        //设置昵称
        return memberInfoService.updateNickname(updateNicknameReq);
    }

    @PostMapping("/setNewPaymentPassword")
    @ApiOperation(value = "前台-设置新支付密码")
    @LogMemberOperation(value = MemberOperationModuleEnum.SET_NEW_PAYMENT_PASSWORD)
    public RestResult setNewPaymentPassword(@RequestBody @ApiParam @Valid NewPaymentPasswordReq newPaymentPasswordReq) {
        //设置新支付密码
        return memberInfoService.setNewPaymentPassword(newPaymentPasswordReq);
    }

    @PostMapping("/updatePaymentPassword")
    @ApiOperation(value = "前台-修改支付密码")
    @LogMemberOperation(value = MemberOperationModuleEnum.CHANGE_PAYMENT_PASSWORD)
    public RestResult updatePaymentPassword(@RequestBody @ApiParam @Valid UpdatePaymentPasswordReq updatePaymentPasswordReq) {
        //修改支付密码
        return memberInfoService.updatePaymentPassword(updatePaymentPasswordReq);
    }

    @GetMapping("/verificationStatus")
    @ApiOperation(value = "前台-查看会员交易状态")
    public RestResult<verificationStatusVo> verificationStatus() {
        //查看会员交易状态
        return memberInfoService.verificationStatus();
    }


    @GetMapping("/getUsdtCurrencyAndPayType")
    @ApiOperation(value = "前台-获取USDT汇率和支付类型")
    public RestResult<UsdtCurrencyAndPayTypeVo> getUsdtCurrencyAndPayType() {
        //获取USDT汇率和支付类型
        return memberInfoService.getUsdtCurrencyAndPayType();
    }

    @PostMapping("/viewMyAppeal")
    @ApiOperation(value = "前台-我的申诉")
    public RestResult<PageReturn<ViewMyAppealVo>> viewMyAppeal(@RequestBody(required = false) @ApiParam @Valid PageRequestHome pageRequestHome) {
        //我的申诉
        return appealOrderService.viewMyAppeal(pageRequestHome);
    }

    /**
     * 获取当前用户的交易记录
     * 从买入表collection_order 卖出表payment_order usdt充值表中usdt_buy_order 查询出交易记录
     *
     * @param viewTransactionHistoryReq
     * @return
     */
    @PostMapping("/viewTransactionHistory")
    @ApiOperation(value = "前台-交易记录")
    public RestResult<PageReturn<ViewTransactionHistoryVo>> viewTransactionHistory(@RequestBody(required = false) @ApiParam @Valid ViewTransactionHistoryReq viewTransactionHistoryReq) {
        return memberInfoService.viewTransactionHistory(viewTransactionHistoryReq);
    }

    @PostMapping("/resetPaymentPassword")
    @ApiOperation(value = "前台-忘记支付密码")
    @LogMemberOperation(value = MemberOperationModuleEnum.FORGET_PAYMENT_PASSWORD)
    public RestResult resetPaymentPassword(@RequestBody @ApiParam @Valid ResetPaymentPasswordReq resetPaymentPasswordReq) {
        //忘记支付密码处理
        return memberInfoService.resetPaymentPassword(resetPaymentPasswordReq);
    }


    @PostMapping("/finishNewUserGuide")
    @ApiOperation(value = "前台-完成新手引导")
    @LogMemberOperation(value = MemberOperationModuleEnum.FINISH_NEW_USER_GUIDE)
    public RestResult finishNewUserGuide(@RequestBody @ApiParam @Valid NewUserGuideReq newUserGuideReq) {
        //完成新手引导
        return memberInfoService.finishNewUserGuide(newUserGuideReq.getType());
    }

    @PostMapping("/getCreditScoreInfo")
    @ApiOperation(value = "前台-获取用户分信息")
    public RestResult getCreditScoreInfo() {
        return memberInfoService.getCreditScoreInfo();
    }

    @GetMapping("/getLevelInfo")
    @ApiOperation(value = "获取等级信息")
    public RestResult<List<MemberLevelDTO>> getMemberLevelInfo() {
        List<MemberLevelDTO> result = memberInfoService.getMemberLevelInfo();
        return RestResult.ok(result);
    }

    @GetMapping("/getAppVersionInfo")
    @ApiOperation(value = "获取APP版本信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currentVersion", value = "当前版本号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "device", value = "设备：1-ios 2-android", required = true, dataType = "Integer"),
    })
    public RestResult<AppVersionManagerDTO> getAppVersionInfo(
            @RequestParam(value = "currentVersion", required = true) String currentVersion,
            @RequestParam(value = "device", required = true) Integer device
    ) {
        AppVersionManagerDTO result = memberInfoService.getAppVersionInfo(currentVersion, device);
        return RestResult.ok(result);
    }


    /**
     * 获取每日公告内容
     *
     * @return {@link RestResult}<{@link DailyAnnouncementVo}>
     */
    @GetMapping("/getDailyAnnouncement")
    @ApiOperation(value = "获取每日公告内容 ")
    public RestResult<DailyAnnouncementVo> getDailyAnnouncement(
            @RequestParam(value = "language", required = false, defaultValue = "1")
            @ApiParam(value = "语言代码，1表示英语，2表示印地语，不提供则默认为英语") Integer language) {
        return memberInfoService.getDailyAnnouncement(language);
    }


    /**
     * 标记用户已查看今日公告
     *
     * @return {@link RestResult}
     */
    @ApiOperation(value = "标记用户已查看今日公告 ")
    @GetMapping("/markAnnouncementAsViewed")
    public RestResult markAnnouncementAsViewed() {
        return memberInfoService.markAnnouncementAsViewed();
    }

    /**
     * 标记用户退回订单已弹窗
     * @return {@link RestResult}
     */
    @GetMapping("/markCashBackAttention")
    @ApiOperation(value = "前台-余额退回弹窗状态更改")
    public RestResult markCashBackAttention() {
        return memberInfoService.markCashBackAttention();
    }


    @GetMapping("/getBankList")
    @ApiOperation(value = "获取银行列表")
    public RestResult<List<BankInfoVo>> getBankList()  {
        return bankInfoService.getBankList();
    }
}
