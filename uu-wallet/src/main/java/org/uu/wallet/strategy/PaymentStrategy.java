package org.uu.wallet.strategy;

import org.uu.common.core.result.ApiResponse;
import org.uu.wallet.req.ApiRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 定义代付策略接口
 *
 * @author simon
 * @date 2024/07/17
 */
public interface PaymentStrategy {

    /**
     * 处理代收订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse }
     */
    ApiResponse processPayment(ApiRequest apiRequest, HttpServletRequest request);
}