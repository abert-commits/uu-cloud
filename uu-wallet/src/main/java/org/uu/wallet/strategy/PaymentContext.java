package org.uu.wallet.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.strategy.impl.CardPaymentStrategy;
import org.uu.wallet.strategy.impl.TrxPaymentStrategy;
import org.uu.wallet.strategy.impl.UsdtPaymentStrategy;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 代付类型策略上下文
 *
 * @author simon
 * @date 2024/07/15
 */
@Component
@Slf4j
public class PaymentContext {

    private final Map<String, PaymentStrategy> paymentStrategyMap = new HashMap<>();

    /**
     * 通过构造函数注入所有的策略类，并将它们存储到strategyMap中。
     *
     * @param cardPaymentStrategy
     * @param usdtPaymentStrategy
     */
    @Autowired
    public PaymentContext(CardPaymentStrategy cardPaymentStrategy,
                          UsdtPaymentStrategy usdtPaymentStrategy,
                          TrxPaymentStrategy trxPaymentStrategy) {
        paymentStrategyMap.put(PayTypeEnum.INDIAN_CARD.getCode(), cardPaymentStrategy);
        paymentStrategyMap.put(PayTypeEnum.INDIAN_USDT.getCode(), usdtPaymentStrategy);
        paymentStrategyMap.put(PayTypeEnum.INDIAN_TRX.getCode(), trxPaymentStrategy);
    }

    /**
     * 根据不同的支付类型选择不同的处理方式
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse }
     */
    public ApiResponse executeStrategy(ApiRequest apiRequest, HttpServletRequest request) {
        PaymentStrategy paymentStrategy = paymentStrategyMap.get(apiRequest.getChannel());
        if (paymentStrategy != null) {
            //处理代收订单
            return paymentStrategy.processPayment(apiRequest, request);
        } else {
            //代收处理失败, 未知支付类型
            log.error("API提现接口订单提交失败, 未知支付类型, 商户号: {}, 支付类型: {}", apiRequest.getMerchantCode(), apiRequest.getChannel());
            return ApiResponse.of(ApiResponseEnum.UNSUPPORTED_PAY_TYPE, null);
        }
    }
}
