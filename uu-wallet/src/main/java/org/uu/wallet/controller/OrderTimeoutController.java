package org.uu.wallet.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.service.HandleOrderTimeoutService;
import org.uu.wallet.service.IMatchingOrderService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1/orderTimeOut")
@RequiredArgsConstructor
@ApiIgnore
public class OrderTimeoutController {

    private final HandleOrderTimeoutService handleOrderTimeoutService;
    private final IMatchingOrderService matchingOrderService;

//    /**
//     * 支付超时处理
//     *
//     * @param platformOrder
//     * @return {@link Boolean}
//     */
//    @PostMapping("/paymentTimeout")
//    public Boolean handlePaymentTimeout(@RequestParam("platformOrder") String platformOrder) {
//        return handleOrderTimeoutService.handlePaymentTimeout(platformOrder);
//    }

    /**
     * USDT支付超时处理
     *
     * @param platformOrder
     * @return {@link Boolean}
     */
//    @PostMapping("/usdtPaymentTimeout")
//    public Boolean handleUsdtPaymentTimeout(@RequestParam("platformOrder") String platformOrder) {
//        return handleOrderTimeoutService.handleUsdtPaymentTimeout(platformOrder);
//    }
}
