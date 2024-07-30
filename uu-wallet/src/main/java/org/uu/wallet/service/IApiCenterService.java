package org.uu.wallet.service;


import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.bo.UsdtPaymentInfoBO;
import org.uu.wallet.entity.PaymentInfo;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.req.ApiRequestQuery;
import org.uu.wallet.req.ConfirmPaymentReq;

import javax.servlet.http.HttpServletRequest;

public interface IApiCenterService {

    /**
     * 充值接口
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    ApiResponse depositApply(ApiRequest apiRequest, HttpServletRequest request);


    /**
     * 提现接口
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    ApiResponse withdrawalApply(ApiRequest apiRequest, HttpServletRequest request);


    /**
     * 获取支付页面(收银台)信息接口
     *
     * @param token
     * @return {@link RestResult}<{@link PaymentInfo}>
     */
    RestResult<PaymentInfo> retrievePaymentDetails(String token);


    /**
     * 收银台 确认支付 提交接口
     *
     * @param confirmPaymentReq
     * @return {@link RestResult}
     */
    RestResult confirmPayment(ConfirmPaymentReq confirmPaymentReq);

    /**
     * 查询充值订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    ApiResponse depositQuery(ApiRequestQuery apiRequest, HttpServletRequest request);


    /**
     * 查询提现订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    ApiResponse withdrawalQuery(ApiRequestQuery apiRequest, HttpServletRequest request);

    String testDepositApply(HttpServletRequest request, String channel);

    /**
     * 获取USDT支付页面
     *
     * @param token
     * @return {@link RestResult }<{@link UsdtPaymentInfoBO }>
     */
    RestResult<UsdtPaymentInfoBO> retrieveUsdtPaymentDetails(String token);
}
