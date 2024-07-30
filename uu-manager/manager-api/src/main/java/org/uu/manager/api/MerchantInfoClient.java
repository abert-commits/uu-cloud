package org.uu.manager.api;

import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "merchant-info")
public interface MerchantInfoClient {


    /**
     * 获取商户信息
     *
     * @param userId
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/current")
    RestResult<MerchantInfoDTO> fetchMerchantInfo(@RequestParam(value = "userId") Long userId);

    /**
     * 修改商户登录密码
     *
     * @param merchantInfoPwdReq
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/updatePwd")
    RestResult updateMerchantPwd(@RequestBody MerchantInfoPwdReq merchantInfoPwdReq);


    /**
     * 修改商户提现usdt地址
     *
     * @param id
     * @param usdtAddress
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/updateUsdtAddress")
    RestResult updateUsdtAddress(@RequestParam(value = "id") Long id, @RequestParam(value = "usdtAddress") String usdtAddress);

    /**
     * 商户后台手动下分
     *
     * @param merchantCode
     * @param amount
     * @param currency
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/merchantWithdraw")
    RestResult merchantWithdraw(@RequestParam(value = "merchantCode") String merchantCode,
                                @RequestParam(value = "amount") BigDecimal amount,
                                @RequestParam(value = "currency") String currency,
                                @RequestParam(value = "remark") String remark,
                                @RequestParam(value = "payType") String payType);


    @PostMapping("/api/v1/merchantinfo/createMerchantInfo")
    RestResult<MerchantInfoAddDTO> createMerchantInfo(@RequestBody MerchantInfoAddReq merchantInfoReq);


    /**
     * 获取商户信息
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/updateForAdmin")
    RestResult<MerchantInfoAddDTO> updateForAdmin(@RequestBody MerchantInfoUpdateReq merchantInfoReq);


    /**
     * 获取商户列表
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/listpage")
    RestResult<List<MerchantInfoListPageDTO>> listPage(@RequestBody MerchantInfoListPageReq merchantInfoReq);


    /**
     * 获取商户列表
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/delete")
    RestResult<MerchantInfoDTO> delete(@RequestBody MerchantInfoDeleteReq req);

    @PostMapping("/api/v1/merchantinfo/getInfo")
    RestResult<MerchantInfoDTO> getInfo(@RequestBody MerchantInfoGetInfoReq req);


    @PostMapping("/api/v1/merchantinfo/applyRecharge")
    RestResult<ApplyDistributedDTO> applyRecharge(@RequestBody ApplyDistributedReq req);

    @PostMapping("/api/v1/merchantinfo/applyWithdraw")
    RestResult<ApplyDistributedDTO> applyWithdraw(@RequestBody ApplyDistributedReq req);


    @PostMapping("/api/v1/merchantinfo/resetPassword")
    RestResult resetPassword(@RequestParam("code") String code);

    @PostMapping("/api/v1/merchantinfo/resetKey")
    RestResult resetKey(@RequestParam("code") String code);


    /**
     * 获取商户首页信息
     *
     * @param merchantId
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/homePage")
    RestResult<MerchantFrontPageDTO> fetchHomePageInfo(@RequestParam(value = "merchantId") Long merchantId,
                                                       @RequestParam(value = "name") String name);

    /**
     * 获取代付订单列表
     *
     * @param withdrawOrderReq
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/fetchWithdrawOrderInfo")
    RestResult<List<WithdrawOrderDTO>> fetchWithdrawOrderInfo(@RequestBody WithdrawOrderReq withdrawOrderReq);

    /**
     * 获取代付订单列表
     *
     * @param closeWithdrawOrderReq
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/closePaymentOrder")
    RestResult<Boolean> closePaymentOrder(@RequestBody PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/fetchWithdrawOrderInfoExport")
    RestResult<List<WithdrawOrderExportDTO>> fetchWithdrawOrderInfoExport(@RequestBody WithdrawOrderReq withdrawOrderReq);


    /**
     * 手动回调成功
     *
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/confirmSuccess")
    RestResult<String> confirmSuccess(@RequestParam(value = "id") Long id);

    /**
     * 获取订单状态
     *
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/orderStatus")
    RestResult<Map<Integer, String>> fetchOrderStatus();

    /**
     * 获取订单回调状态
     *
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/orderCallbackStatus")
    RestResult<Map<Integer, String>> orderCallbackStatus();


    /**
     * 代收手动回调成功
     *
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/rechargeConfirmSuccess")
    RestResult<Boolean> rechargeConfirmSuccess(@RequestParam(value = "id") Long id);

    /**
     * 获取代收订单列表
     *
     * @param withdrawOrderReq
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/fetchRechargeOrderInfo")
    RestResult<List<RechargeOrderDTO>> fetchRechargeOrderInfo(@RequestBody RechargeOrderReq withdrawOrderReq);

    @PostMapping("/api/v1/merchantinfo/fetchRechargeOrderInfoExport")
    RestResult<List<RechargeOrderExportDTO>> fetchRechargeOrderInfoExport(@RequestBody RechargeOrderReq withdrawOrderReq);

    @PostMapping("/api/v1/merchantinfo/getMerchantName")
    RestResult<Map<Integer, String>> getMerchantName();

    @PostMapping("/api/v1/merchantinfo/getCurrency")
    RestResult<Map<String, String>> getCurrency();

    @PostMapping("/api/v1/merchantinfo/overview")
    RestResult<MerchantFrontPageDTO> fetchOverviewInfo();

    @PostMapping("/api/v1/merchantinfo/todayOrderOverview")
    RestResult<TodayOrderOverviewDTO> todayOrderOverview();

    @PostMapping("/api/v1/merchantinfo/todayUsdtOrderOverview")
    RestResult<TodayUsdtOrderOverviewDTO> todayUsdtOrderOverview();

    @PostMapping("/api/v1/merchantinfo/updateMerchantPublicKey")
    RestResult updateMerchantPublicKey(@RequestParam(value = "id") Long id, @RequestParam(value = "merchantPublicKey") String merchantPublicKey);

    @PostMapping("/api/v1/merchantinfo/validGoogle")
    RestResult<Boolean> validGoogle(@RequestParam(value = "totpCode") String totpCode);

    @PostMapping("/api/v1/merchantinfo/resetMerchantGoogle")
    RestResult resetMerchantGoogle(@RequestParam(value = "merchantCode") String merchantCode);

    /**
     * 获取订单数量概览
     *
     * @return OrderOverviewDTO
     */
    @PostMapping("/api/v1/merchantinfo/getOrderNumOverview")
    RestResult<OrderOverviewDTO> getOrderNumOverview();

    @PostMapping("/api/v1/merchantinfo/getMemberOverview")
    RestResult<MemberOverviewDTO> getMemberOverview();


    /**
     * 获取代收/代付订单统计概览
     *
     * @return
     */
    @PostMapping("/api/v1/merchantinfo/getMerchantOrderOverview")
    RestResult<MerchantOrderOverviewDTO> getMerchantOrderOverview(@RequestBody CommonDateLimitReq commonDateLimitReq);

    @PostMapping("/api/v1/merchantinfo/getLatestOrderTime")
    List<MerchantLastOrderWarnDTO> getLatestOrderTime();

    @PostMapping("/api/v1/merchantinfo/paid")
    KycRestResult paid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/unPaid")
    KycRestResult unPaid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/usdtPaid")
    KycRestResult usdtPaid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/usdtUnPaid")
    KycRestResult usdtUnPaid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/manualConfirmation")
    KycRestResult manualConfirmation(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/merchantinfo/manualCancel")
    KycRestResult manualCancel(@RequestBody @ApiParam PaidParamReq req);

}
