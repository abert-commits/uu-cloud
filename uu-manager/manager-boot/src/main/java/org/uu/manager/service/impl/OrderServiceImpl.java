package org.uu.manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.OrderStatusOverviewDTO;
import org.uu.common.pay.dto.OrderStatusOverviewListDTO;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.service.IBiPaymentOrderService;
import org.uu.manager.service.IBiWithdrawOrderDailyService;
import org.uu.manager.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author admin
 * @date 2024/3/15 14:15
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final IBiPaymentOrderService biPaymentOrderService;
    private final IBiWithdrawOrderDailyService iBiWithdrawOrderDailyService;

    @Override
    public RestResult<OrderStatusOverviewListDTO> getOrderStatusOverview(CommonDateLimitReq req) {
        OrderStatusOverviewListDTO result = new OrderStatusOverviewListDTO();
        // 查询买入订单信息
        BiPaymentOrder biPaymentOrder = biPaymentOrderService.getPaymentOrderStatusOverview(req);
        OrderStatusOverviewDTO paymentOrderStatusOverview = getOrderStatusOverviewDTO(biPaymentOrder);
        result.setBuyOrderOverview(paymentOrderStatusOverview);
        // 查询卖出订单信息
        BiWithdrawOrderDaily biWithdrawOrderDaily = iBiWithdrawOrderDailyService.getWithdrawOrderStatusOverview(req);
        OrderStatusOverviewDTO withdrawOrderStatusOverview = getOrderStatusOverviewDTO(biWithdrawOrderDaily);
        result.setSellOrderOverview(withdrawOrderStatusOverview);
        // 计算合计订单信息
        OrderStatusOverviewDTO total = getOrderStatusOverviewDTO(paymentOrderStatusOverview, withdrawOrderStatusOverview);
        result.setTotalOrderOverview(total);
        return RestResult.ok(result);
    }

    private OrderStatusOverviewDTO getOrderStatusOverviewDTO(BiPaymentOrder biPaymentOrder) {
        OrderStatusOverviewDTO result = new OrderStatusOverviewDTO();
        // 确认超时
        result.setConfirmOverTimeNum(biPaymentOrder.getConfirmOverTimeTotal());
        // 取消支付
        result.setCancelPayNum(biPaymentOrder.getCancelPayTotal());
        // 支付超时
        result.setPayOverTimeNum(biPaymentOrder.getPayOverTimeTotal());
        // 订单申诉-成功
        result.setOrderAppealSuccessNum(biPaymentOrder.getAppealSuccessTotal());
        // 订单申诉-失败
        result.setOrderAppealFailedNum(biPaymentOrder.getAppealFailTotal());
        // 订单申诉-合计
        long appealSuccess = biPaymentOrder.getAppealSuccessTotal();
        long appealFail = biPaymentOrder.getAppealFailTotal();
        // 金额错误
        long appealAmountError = biPaymentOrder.getAmountErrorTotal();
        result.setAmountErrorNum(appealAmountError);
        // 申诉订单-合计
        long appealTotal = appealSuccess + appealFail + appealAmountError;
        result.setAppealTotalNum(appealTotal);
        // 已取消
        result.setCancelNum(biPaymentOrder.getCancelOrderTotal());
        // 已完成
        long successOrderNumTotal = biPaymentOrder.getSuccessOrderNumTotal();
        result.setFinishNum(successOrderNumTotal);
        // 计算成功率 成功订单数/总订单数
        long orderTotal = biPaymentOrder.getOrderNumTotal();
        result.setSuccessRate(getSuccessRate(successOrderNumTotal, orderTotal));
        return result;
    }

    private OrderStatusOverviewDTO getOrderStatusOverviewDTO(BiWithdrawOrderDaily biWithdrawOrderDaily) {
        OrderStatusOverviewDTO result = new OrderStatusOverviewDTO();
        result.setMatchOverTimeNum(biWithdrawOrderDaily.getOverTimeNumTotal());
        result.setConfirmOverTimeNum(biWithdrawOrderDaily.getConfirmOverTimeTotal());
        long appealSuccess = biWithdrawOrderDaily.getAppealSuccessTotal();
        long appealFail = biWithdrawOrderDaily.getAppealFailTotal();
        long amountError = biWithdrawOrderDaily.getAmountErrorTotal();
        long appealTotal = appealSuccess + appealFail + amountError;
        result.setOrderAppealSuccessNum(appealSuccess);
        result.setOrderAppealFailedNum(appealFail);
        result.setAmountErrorNum(amountError);
        result.setAppealTotalNum(appealTotal);
        result.setCancelNum(biWithdrawOrderDaily.getCancelOrderTotal());
        long successOrderNumTotal = biWithdrawOrderDaily.getSuccessOrderNumTotal();
        result.setFinishNum(successOrderNumTotal);
        long orderTotal = biWithdrawOrderDaily.getOrderNumTotal();
        result.setSuccessRate(getSuccessRate(successOrderNumTotal, orderTotal));
        return result;
    }

    private OrderStatusOverviewDTO getOrderStatusOverviewDTO(OrderStatusOverviewDTO paymentOrderStatusOverview, OrderStatusOverviewDTO withdrawOrderStatusOverview) {
        OrderStatusOverviewDTO result = new OrderStatusOverviewDTO();
        // 匹配超时
        long matchOverTimeNum = com(paymentOrderStatusOverview.getMatchOverTimeNum(), withdrawOrderStatusOverview.getMatchOverTimeNum());
        result.setMatchOverTimeNum(matchOverTimeNum);
        // 取消支付
        long payCancel = 0L;
        if (Objects.nonNull(paymentOrderStatusOverview.getCancelPayNum())) {
            payCancel = paymentOrderStatusOverview.getCancelPayNum();
        }
        result.setCancelPayNum(payCancel);
        // 支付超时
        long payOverTime = 0L;
        if (Objects.nonNull(paymentOrderStatusOverview.getPayOverTimeNum())) {
            payOverTime = paymentOrderStatusOverview.getPayOverTimeNum();
        }
        result.setPayOverTimeNum(payOverTime);
        // 确认超时
        long confirmOverTimeNum = com(paymentOrderStatusOverview.getConfirmOverTimeNum(), withdrawOrderStatusOverview.getConfirmOverTimeNum());
        result.setConfirmOverTimeNum(confirmOverTimeNum);
        // 申诉成功
        long appealSuccess = com(paymentOrderStatusOverview.getOrderAppealSuccessNum(), withdrawOrderStatusOverview.getOrderAppealSuccessNum());
        result.setOrderAppealSuccessNum(appealSuccess);
        // 申诉失败
        long appealFail = com(paymentOrderStatusOverview.getOrderAppealFailedNum(), withdrawOrderStatusOverview.getOrderAppealFailedNum());
        result.setOrderAppealFailedNum(appealFail);
        // 金额错误
        long amountError = com(paymentOrderStatusOverview.getAmountErrorNum(), withdrawOrderStatusOverview.getAmountErrorNum());
        result.setAmountErrorNum(amountError);
        long appealTotalNum = com(paymentOrderStatusOverview.getAppealTotalNum(), withdrawOrderStatusOverview.getAppealTotalNum());
        result.setAppealTotalNum(appealTotalNum);
        // 已取消
        long cancel = com(paymentOrderStatusOverview.getCancelNum(), withdrawOrderStatusOverview.getCancelNum());
        result.setCancelNum(cancel);
        // 已完成
        long finish = com(paymentOrderStatusOverview.getFinishNum(), withdrawOrderStatusOverview.getFinishNum());
        result.setFinishNum(finish);
        return result;
    }

    private long com(Long paymentNum, Long withdrawNum) {
        long paymentResult = 0L;
        long withdrawResult = 0L;
        if (Objects.nonNull(paymentNum)) {
            paymentResult = paymentNum;
        }
        if (Objects.nonNull(withdrawNum)) {
            withdrawResult = withdrawNum;
        }
        return paymentResult + withdrawResult;
    }

    private BigDecimal getSuccessRate(long successOrderNumTotal, long orderTotal) {
        if (successOrderNumTotal != 0 && orderTotal != 0) {
            return new BigDecimal(successOrderNumTotal).divide(new BigDecimal(orderTotal), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
