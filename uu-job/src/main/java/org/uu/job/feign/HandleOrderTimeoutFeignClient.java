package org.uu.job.feign;

import io.swagger.annotations.ApiOperation;
import org.uu.common.core.result.RestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "uu-wallet", contextId = "order-timeout")
public interface HandleOrderTimeoutFeignClient {

    /**
     * 钱包用户确认超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/walletUserConfirmationTimeout")
    Boolean walletUserConfirmationTimeout(@RequestParam("platformOrder") String platformOrder);


    /**
     * 商户会员确认超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/merchantMemberConfirmationTimeout")
    Boolean merchantMemberConfirmationTimeout(@RequestParam("platformOrder") String platformOrder);


    /**
     * 钱包用户卖出匹配超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/walletUserSaleMatchTimeout")
    Boolean walletUserSaleMatchTimeout(@RequestParam("platformOrder") String platformOrder, @RequestParam("lastUpdateTimestamp") Long lastUpdateTimestamp);


    /**
     * 商户会员卖出匹配超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/merchantMemberSaleMatchTimeout")
    Boolean merchantMemberSaleMatchTimeout(@RequestParam("platformOrder") String platformOrder, @RequestParam("lastUpdateTimestamp") Long lastUpdateTimestamp);


    /**
     * 支付超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/paymentTimeout")
    Boolean paymentTimeout(@RequestParam("platformOrder") String platformOrder);


    /**
     * USDT支付超时处理
     *
     * @return {@link Boolean}
     */
    @PostMapping("/api/v1/orderTimeOut/usdtPaymentTimeout")
    Boolean usdtPaymentTimeout(@RequestParam("platformOrder") String platformOrder);

    /**
     * 取消员确认超时一定时间的订单
     *
     * @return
     */
    @GetMapping("/api/v1/orderTimeOut/cancelConfirmTimeoutOrder")
    @ApiOperation(value = "取消员确认超时一定时间的订单")
    RestResult cancelConfirmTimeoutOrder(@RequestParam(value = "startDays", required = false) Integer startDays);
}
