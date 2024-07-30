package org.uu.wallet.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.wallet.Enum.MemberOperationModuleEnum;
import org.uu.wallet.Enum.SwitchIdEnum;
import org.uu.wallet.annotation.LogMemberOperation;
import org.uu.wallet.dto.GenerateTokenForWallertDTO;
import org.uu.wallet.req.*;
import org.uu.wallet.service.IControlSwitchService;
import org.uu.wallet.service.ICustomerServiceSystemsService;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/signUp"})
@Api(description = "前台-会员注册控制器")
@Validated
public class SignUpController {

    private final IMemberInfoService memberInfoService;

    private final ICustomerServiceSystemsService customerServiceSystemsService;

    private final IControlSwitchService controlSwitchService;

    @Autowired
    private OrderNumberGeneratorUtil orderNumberGenerator;

    @PostMapping("/phoneSignUp")
    @ApiOperation(value = "前台-手机号码注册")
    @LogMemberOperation(value = MemberOperationModuleEnum.PHONE_NUMBER_REGISTRATION)
    public RestResult<PhoneSignUpVo> phoneSignUp(@RequestBody @ApiParam @Valid PhoneSignUpReq phoneSignUpReq, HttpServletRequest request) {
        //手机号码注册处理
        GenerateTokenForWallertDTO generateTokenForWallertDTO = memberInfoService.phoneSignUp(phoneSignUpReq, request);

        //注册成功自动登录
        if (generateTokenForWallertDTO != null) {
            return memberInfoService.generateTokenForWallet(generateTokenForWallertDTO);
        }

        return RestResult.failed();
    }

    @PostMapping("/emailSignUp")
    @ApiOperation(value = "前台-邮箱账号注册")
    @LogMemberOperation(value = MemberOperationModuleEnum.EMAIL_ACCOUNT_REGISTRATION)
    public RestResult emailSignUp(@RequestBody @ApiParam @Valid EmailSignUpReq emailSignUpReq, HttpServletRequest request) {
        //邮箱账号注册处理
        return memberInfoService.emailSignUp(emailSignUpReq, request);
    }

    @PostMapping("/resetPassword")
    @ApiOperation(value = "前台-忘记密码")
    @LogMemberOperation(value = MemberOperationModuleEnum.FORGET_PASSWORD)
    public RestResult resetPassword(@RequestBody @ApiParam @Valid ResetPasswordReq resetPasswordReq) {
        //忘记密码处理
        return memberInfoService.resetPasswordProcess(resetPasswordReq);
    }

    @PostMapping("/sendSmsCode")
    @ApiOperation(value = "前台-发送短信验证码")
    @LogMemberOperation(value = MemberOperationModuleEnum.SEND_SMS_CODE)
    public RestResult sendSmsCode(@RequestBody @ApiParam @Valid SendSmsCodeReq sendSmsCodeReq, HttpServletRequest request) {
        //发送短信验证码
        return memberInfoService.sendSmsCode(sendSmsCodeReq, request);
    }

    @PostMapping("/sendEmailCode")
    @ApiOperation(value = "前台-发送邮箱验证码")
    @LogMemberOperation(value = MemberOperationModuleEnum.SEND_EMAIL_CODE)
    public RestResult sendEmailCode(@RequestBody @ApiParam @Valid SendEmailCodeReq sendEmailCodeReq, HttpServletRequest request) {
        //发送邮箱验证码
        return memberInfoService.sendEmailCode(sendEmailCodeReq, request);
    }

    @PostMapping("/registerVerifySmsCode")
    @ApiOperation(value = "前台-校验短信验证码(仅供注册使用)")
    @LogMemberOperation(value = MemberOperationModuleEnum.VERIFY_SMS_CODE)
    public RestResult sendSmsCode(@RequestBody @ApiParam @Valid ValidateSmsCodeReq validateSmsCodeReq) {
        //校验短信验证码
        return memberInfoService.signUpValidateSmsCode(validateSmsCodeReq) ? RestResult.ok() : RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
    }

    @PostMapping("/testImageRecognition")
    @ApiOperation(value = "图片识别")
    public RestResult<TestImageRecognitionVo> sendSmsCode(@ApiParam(value = "图片", required = true) @RequestPart("image") MultipartFile image) {
        //图片识别


        String base64Encoded = "";
        try {
            // 获取文件的字节数组
            byte[] bytes = image.getBytes();
            // 使用 Base64 编码器将字节数组转换为 Base64 编码的字符串
            base64Encoded = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("图片识别失败, 将图片base64编码错误, e: {}", e);
            e.printStackTrace();
            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        }


        //请求地址
        String url = "http://api-img-yd.fengkongcloud.com/image/v4";

        //data数据
        JSONObject data = new JSONObject();
        data.put("tokenId", UUID.randomUUID().toString());
        data.put("img", base64Encoded);
        data.put("imgCompareBase", "");
        data.put("lang", "en");

        //请求参数
        JSONObject req = new JSONObject();

        req.put("accessKey", "j8Nmcu8CvDNfFqDagMYg");
        req.put("appId", "default");
        req.put("eventId", "article");
        req.put("type", "EROTIC_ADVERT");
//        req.put("businessType","");
        req.put("data", data);
//        req.put("callback","");
        req.put("lang", "en");
        req.put("acceptLang", "en");


        //发送请求
        String res = RequestUtil.HttpRestClientToJson(url, JSON.toJSONString(req));


        TestImageRecognitionVo testImageRecognitionVo = new TestImageRecognitionVo();

        JSONObject jsonObject = JSON.parseObject(res);

        if ("1100".equals(jsonObject.getString("code"))) {
            log.info("图片识别成功, res: {}", res);
            //请求成功

            //图片识别结果
            testImageRecognitionVo.setRiskLabel1(jsonObject.getString("riskLabel1"));

            JSONObject riskDetail = jsonObject.getJSONObject("riskDetail");
            JSONObject ocrText = riskDetail.getJSONObject("ocrText");
            //图片内容
            testImageRecognitionVo.setOcrText(ocrText.getString("text"));

            //requestId
            testImageRecognitionVo.setRequestId(jsonObject.getString("requestId"));


            //riskLevel
            testImageRecognitionVo.setRiskLevel(jsonObject.getString("riskLevel"));

            //AI接口返回数据
            testImageRecognitionVo.setRes(jsonObject);

            return RestResult.ok(testImageRecognitionVo);
        } else {
            log.info("图片识别失败, res: {}", res);
            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        }
    }


    /**
     * 前台-获取当前客服系统
     *
     * @return {@link RestResult}<{@link BannerListVo}>
     */
    @GetMapping("/getCurrentCustomerServiceSystem")
    @ApiOperation(value = "前台-获取当前客服系统")
    public RestResult<CurrentCustomerServiceSystemVo> getCurrentCustomerServiceSystem() {
        return customerServiceSystemsService.getCurrentCustomerServiceSystem();
    }


//    @GetMapping("/tbPay")
//    public String tbPay() {
//
//        String url = "https://api.tbtestsever.com/apiOrder/pay";
//
//        String key = "S43TNULZUA2DO6JX";
//
//        TreeMap<String, Object> parms = new TreeMap<>();
//
//        parms.put("merchantNo", "2000041");
//        parms.put("payType", "3");
//
//        parms.put("orderNo", orderNumberGenerator.generateOrderNo("AR"));
//        parms.put("notifyUrl", "https://google.com");
//        parms.put("orderAmount", "1000.00");
//
//        parms.put("version", "1.0");
//        parms.put("goods", "apple");
//        parms.put("memberId", "tb001");
//
//
//        String jsonString = JSON.toJSONString(parms);
//
//
//
//        final String sign = AESUtils.encrypt(jsonString, key);
//
//        parms.put("sign", sign);
//
//        String jsonString2 = JSON.toJSONString(parms);
//
//        parms.put("signType", "AES");
//
//        String res = RequestUtil.HttpRestClientToJson(url, jsonString2);
//
//        System.out.println(res);
//
//        return res;
//    }


    /**
     * 前台-获取当前客服系统
     *
     * @return {@link RestResult}<{@link BannerListVo}>
     */
    @PostMapping("/checkPhoneNumberAvailability")
    @ApiOperation(value = "前台-校验手机号是否被使用")
    public RestResult<CheckPhoneNumberAvailabilityVo> checkPhoneNumberAvailability(@RequestBody @ApiParam @Valid CheckPhoneNumberAvailabilityReq checkPhoneNumberAvailabilityReq) {
        return memberInfoService.checkPhoneNumberAvailability(checkPhoneNumberAvailabilityReq);
    }


    @ApiOperation(value = "查询注册相关开关状态")
    @GetMapping("/getRegistrationSwitchStatus")
    public RestResult<RegistrationSwitchStatusVo> getRegistrationSwitchStatus() {
        RegistrationSwitchStatusVo switchStatus = new RegistrationSwitchStatusVo();
        switchStatus.setMobileRegistrationCaptchaEnabled(controlSwitchService.isSwitchEnabled(SwitchIdEnum.REGISTRATION_CAPTCHA.getSwitchId()));
        switchStatus.setInvitationCodeRegistrationEnabled(controlSwitchService.isSwitchEnabled(SwitchIdEnum.INVITATION_CODE_REGISTRATION.getSwitchId()));

        return RestResult.ok(switchStatus);
    }

//    @PostMapping("/test1")
//    public List<BankKycTransactionVo> test(@RequestBody @ApiParam @Valid LinkKycPartnerReq linkKycPartnerReq) {
//
//
//        IAppBankTransaction appBankTransaction = SpringContextUtil.getBean("freeCharge");
//
//        List<BankKycTransactionVo> kycBankTransactions = appBankTransaction.getKYCBankTransactions(linkKycPartnerReq.getToken());
//
//
//
//        return kycBankTransactions;
//    }


//    @GetMapping("/test1")
//    public List<BankKycTransactionVo> test() {
//
//        KycTransactionMessage kycTransactionMessage = new KycTransactionMessage();
//
//        kycTransactionMessage.setBuyerMemberId(95511L);
//
//        kycTransactionMessage.setBuyerMemberAccount("91133400001");
//
//        kycTransactionMessage.setSellerMemberId(95771L);
//
//        kycTransactionMessage.setSellerMemberAccount("77777777");
//
//        kycTransactionMessage.setRecipientUPI("8891703604@ikwik");
//
//        kycTransactionMessage.setAmount(BigDecimal.valueOf(100.00));
//
//        kycTransactionMessage.setTransactionUTR("412432628103");
//
//        kycTransactionMessage.setTransactionTime(LocalDateTime.now());
//
//        kycTransactionMessage.setBuyerOrderId("MR2024050318190008190");
//
//        kycTransactionMessage.setSellerOrderId("MC2024050317491808181");
//
//        // 通过 KYC 验证完成订单
//        Boolean finalizeOrderWithKYCVerification = kycCenterService.finalizeOrderWithKYCVerification(kycTransactionMessage);
//
//
//        return null;
//    }
}
