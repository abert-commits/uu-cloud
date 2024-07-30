//package org.uu.manager.controller;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.constant.GlobalConstants;
//import org.uu.common.core.page.PageReturn;
//import org.uu.common.core.result.RestResult;
//import org.uu.common.core.result.ResultCode;
//import org.uu.common.core.utils.CommonUtils;
//import org.uu.common.pay.dto.UserAuthDTO;
//import org.uu.common.pay.req.MerchantInfoPwdReq;
//import org.uu.common.web.exception.BizException;
//import org.uu.common.web.utils.UserContext;
//import org.uu.wallet.Enum.AccountChangeEnum;
//import org.uu.wallet.Enum.ChangeModeEnum;
//import org.uu.wallet.entity.MerchantInfo;
//import org.uu.wallet.req.MerchantInfoReq;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.uu.wallet.service.IReportCollectionOrderService;
//import org.uu.wallet.vo.MerchantInfoVo;
//import org.uu.wallet.vo.MerchantNameListVo;
//import org.springframework.beans.BeanUtils;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//
//@RequestMapping(value = {"/api/v1/reportMerchantInfo", "/reportMerchantInfo"})
//@Api(description = "商户控制器")
//public class ReportMerchantInfoController {
//
//    private final IMerchantInfoService merchantInfoService;
//    private final PasswordEncoder passwordEncoder;
//    private final IReportCollectionOrderService reportCollectionOrderService;
//
//
//    @PostMapping("/createMerchantInfo")
//    @ApiOperation(value = "创建商户")
//    public RestResult<MerchantInfo> save(@RequestBody @ApiParam MerchantInfoVo merchantInfoVo) {
//        String passwd = passwordEncoder.encode(GlobalConstants.USER_DEFAULT_PASSWORD);
//        MerchantInfo merchantInfo = new MerchantInfo();
//        BeanUtils.copyProperties(merchantInfoVo, merchantInfo);
//        merchantInfo.setPassword(passwd);
//        merchantInfo.setStatus("1");
//        merchantInfo.setDeleted("0");
//        merchantInfoService.save(merchantInfo);
//
//        return RestResult.ok();
//    }
//
//
//
//    @PostMapping("/update")
//    @ApiOperation(value = "更新商户信息")
//    public RestResult update(@RequestBody @ApiParam MerchantInfoReq merchantInfoReq) {
//        Long currentUserId = UserContext.getCurrentUserId();
//        if (!merchantInfoReq.getId().equals(currentUserId)) return RestResult.ok("必须当前用户才能修改");
//        MerchantInfo merchantInfo = new MerchantInfo();
//        BeanUtils.copyProperties(merchantInfoReq, merchantInfo);
//        boolean su = merchantInfoService.updateById(merchantInfo);
//        return RestResult.ok();
//
//    }
//
//    @PostMapping("/updatePwd")
//    @ApiOperation(value = "修改商户登录密码")
//    public RestResult update(@RequestBody MerchantInfoPwdReq merchantInfoPwdReq) {
//
//
//        if(!merchantInfoPwdReq.getNewPwd().equals(merchantInfoPwdReq.getConfirmNewPwd())){
//            throw new BizException(ResultCode.MERCHANT_PASSWORDS_INCONSISTENT);
//        }
//
//        // 校验原始密码是否正确
//        MerchantInfo merchantInfo =  merchantInfoService.userDetail(merchantInfoPwdReq.getId());
//        boolean result = passwordEncoder.matches(merchantInfoPwdReq.getOriginalPwd(), merchantInfo.getPassword());
//        if(!result){
//            throw new BizException(ResultCode.MERCHANT_ORIGINAL_PASSWORDS_WRONG);
//        }
//        String newPwd = passwordEncoder.encode(merchantInfoPwdReq.getNewPwd());
//        merchantInfoService.updateMerchantPwd(merchantInfoPwdReq.getId(), newPwd, merchantInfoPwdReq.getPwdTips());
//
//        return RestResult.ok();
//
//    }
//
//    @PostMapping("/listpage")
//    @ApiOperation(value = "获取商户列表")
//    public RestResult list(@RequestBody @ApiParam MerchantInfoReq merchantInfoReq) {
//        PageReturn<MerchantInfo> payConfigPage = merchantInfoService.listPage(merchantInfoReq);
//        return RestResult.page(payConfigPage);
//    }
//
//    @PostMapping("/current")
//    @ApiOperation(value = "获取当前商户信息")
//    public RestResult<MerchantInfoVo> currentMerchantInfo(Long userId) {
//        MerchantInfoVo merchantInfo = merchantInfoService.currentMerchantInfo(userId);
//        return RestResult.ok(merchantInfo);
//    }
//
//    @GetMapping("/merchantNameList")
//    @ApiOperation(value = "获取商户名称列表")
//    public RestResult merchantNameList() {
//        List<MerchantNameListVo> payConfigPage = merchantInfoService.getMerchantNameList();
//        return RestResult.ok(payConfigPage);
//    }
//
//
//    /**
//     * 获取会员用户信息
//     */
//    @GetMapping("/merchant/username/{username}")
//    @ApiOperation(value = "获取会员用户信息")
//    public RestResult<UserAuthDTO> getMemberUserByUsername(@PathVariable String username) {
//        log.info("获取member user info。。。");
//        UserAuthDTO user = merchantInfoService.getByUsername(username);
//        return RestResult.ok(user);
//    }
//
//    @PostMapping("/updateUsdtAddress")
//    @ApiOperation(value = "修改商户USDT地址")
//    public RestResult updateUsdtAddress(Long id, String usdtAddress) {
//
//        merchantInfoService.updateUsdtAddress(id, usdtAddress);
//
//        return RestResult.ok();
//
//    }
//
//    /**
//     * 商户后台手动下分
//     * @param merchantCode
//     * @param amount
//     * @param currency
//     * @return
//     */
//    @PostMapping("/merchantWithdraw")
//    @ApiOperation(value = "商户后台手动下分")
//    public RestResult merchantWithdraw(String merchantCode, BigDecimal amount, String currency) {
//
//        String orderNo = CommonUtils.generateOrderNo("XF");
//
//        reportCollectionOrderService.insertChangeAmountRecord(merchantCode, amount, ChangeModeEnum.SUB, currency, orderNo, AccountChangeEnum.WITHDRAW);
//
//        return RestResult.ok();
//
//    }
//
//}
