package org.uu.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.DurationCalculatorUtil;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiPaymentOrderExportDTO;
import org.uu.common.pay.dto.MemberOrderOverviewDTO;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.mapper.BiPaymentOrderMapper;
import org.uu.manager.req.PaymentOrderReportReq;
import org.uu.manager.service.IBiPaymentOrderService;
import org.uu.manager.service.IBiWithdrawOrderDailyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
public class BiPaymentOrderServiceImpl extends ServiceImpl<BiPaymentOrderMapper, BiPaymentOrder> implements IBiPaymentOrderService {

    private final IBiWithdrawOrderDailyService iBiWithdrawOrderDailyService;

    @Override
    @SneakyThrows
    public PageReturn<BiPaymentOrder> listPage(PaymentOrderReportReq req) {

        Page<BiPaymentOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiPaymentOrder> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiPaymentOrder::getDateTime);
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<BiPaymentOrder> queryWrapper = new QueryWrapper<BiPaymentOrder>()
                .select("IFNULL(sum(actual_money), 0) as actualMoneyTotal, " +
                        "IFNULL(sum(proportion_success_orders), 0) as proportionSuccessOrders, " +
                        "IFNULL(sum(proportion_success_amount), 0) as proportionSuccessAmount, " +
                        "IFNULL(sum(buy_bonus), 0) as buyBonusTotal," +
                        "IFNULL(sum(success_order_num), 0) as successOrderNumTotal, " +
                        "IFNULL(sum(order_num), 0) as orderNumTotal, " +
                        "IFNULL(sum(money), 0) as moneyTotal"

                ).lambda();

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiPaymentOrder::getDateTime, req.getStartTime());
            queryWrapper.ge(BiPaymentOrder::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiPaymentOrder::getDateTime, req.getEndTime());
            queryWrapper.le(BiPaymentOrder::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiPaymentOrder::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(BiPaymentOrder::getMerchantCode, req.getMerchantCode());
        }
        Page<BiPaymentOrder> finalPage = page;
        CompletableFuture<BiPaymentOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<BiPaymentOrder>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        BiPaymentOrder totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("actualMoneyTotal", totalInfo.getActualMoneyTotal().toPlainString());
        extent.put("buyBonusTotal", totalInfo.getBuyBonusTotal().toPlainString());
        extent.put("successOrderNumTotal", totalInfo.getSuccessOrderNumTotal());
        extent.put("orderNumTotal", totalInfo.getOrderNumTotal());
        extent.put("moneyTotal", totalInfo.getMoneyTotal());
        long successOrderNumPageTotal = 0L;
        BigDecimal actualMoneyPageTotal = BigDecimal.ZERO;
        long orderNumPageTotal = 0;
        BigDecimal moneyPageTotal = BigDecimal.ZERO;
        BigDecimal buyBonusPageTotal = BigDecimal.ZERO;

        List<BiPaymentOrder> records = page.getRecords();
        for (BiPaymentOrder item : records) {
            if (item.getOrderNum() <= 0L) {
                item.setSuccessRate(0d);
                item.setAverageFinishDuration(0L);
            } else {
                double result = new BigDecimal(item.getSuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                item.setSuccessRate(result);
                Long averageFinishDuration = new BigDecimal(item.getFinishDuration()).
                        divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).longValue();
                item.setAverageFinishDuration(averageFinishDuration);
            }
            actualMoneyPageTotal = actualMoneyPageTotal.add(item.getActualMoney());
            successOrderNumPageTotal = successOrderNumPageTotal + item.getSuccessOrderNum();
            orderNumPageTotal = orderNumPageTotal + item.getOrderNum();
            moneyPageTotal = moneyPageTotal.add(item.getMoney());
            buyBonusPageTotal = buyBonusPageTotal.add(item.getBuyBonus());
            //买入成功订单占比
            Long successOrderNum = item.getSuccessOrderNum();
            Long orderNum = item.getOrderNum();
            double result = 0.00;
            if (successOrderNum > 0 && orderNum > 0) {
                result = (double) successOrderNum / orderNum;
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedNumber = df.format(result);
                //买入成功订单占比
                item.setProportionSuccessOrders(new BigDecimal(formattedNumber).setScale(2, RoundingMode.DOWN));
            }
            //买入成功金额占比
            if (item.getMoney().compareTo(BigDecimal.ZERO) > 0) {
                if (item.getActualMoney().compareTo(item.getMoney()) < 0) {
                    item.setProportionSuccessAmount(item.getActualMoney().divide(item.getMoney(), 2, RoundingMode.DOWN))    ;
                }
            }
        }
        extent.put("actualMoneyPageTotal", actualMoneyPageTotal.toPlainString());
        extent.put("successOrderNumPageTotal", successOrderNumPageTotal);
        extent.put("orderNumPageTotal", orderNumPageTotal);
        extent.put("moneyPageTotal", moneyPageTotal.toPlainString());
        extent.put("buyBonusPageTotal", buyBonusPageTotal.toPlainString());
        return PageUtils.flush(page, records, extent);
    }

    public static void main(String[] args) {
        BigDecimal s = new BigDecimal(420);
        BigDecimal f = new BigDecimal(620);
        System.out.println(s.divide(f));

    }
    @Override
    public PageReturn<BiPaymentOrderExportDTO> listPageForExport(PaymentOrderReportReq req) {
        List<BiPaymentOrderExportDTO> list = new ArrayList<>();
        PageReturn<BiPaymentOrder> biPaymentOrderPageReturn = listPage(req);
        List<BiPaymentOrder> data = biPaymentOrderPageReturn.getList();
        for (BiPaymentOrder item : data) {
            BiPaymentOrderExportDTO biPaymentOrderDTO = new BiPaymentOrderExportDTO();
            BeanUtils.copyProperties(item, biPaymentOrderDTO);
            if (item.getSuccessRate() != null) {
                double successRate = (item.getSuccessRate() * 100);
                String successRateStr = (int) successRate + "%";
                biPaymentOrderDTO.setSuccessRate(successRateStr);
            }
            String orderCompleteDuration = "";
            if (item.getAverageFinishDuration() != null && item.getAverageFinishDuration() != 0) {
                String averageFinishDurationStr = item.getAverageFinishDuration().toString();
                orderCompleteDuration = DurationCalculatorUtil.getOrderCompleteDuration(averageFinishDurationStr);
            }
            biPaymentOrderDTO.setAverageFinishDuration(orderCompleteDuration);
            biPaymentOrderDTO.setActualMoney(item.getActualMoney().toString());
            biPaymentOrderDTO.setTotalFee(item.getTotalFee().toString());
            list.add(biPaymentOrderDTO);
        }
        Page<BiPaymentOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(biPaymentOrderPageReturn.getTotal());
        return PageUtils.flush(page, list);
    }

    @Override
    public BiPaymentOrder getPaymentOrderStatusOverview(CommonDateLimitReq req) {
        LambdaQueryWrapper<BiPaymentOrder> lambdaQuery = new QueryWrapper<BiPaymentOrder>()
                .select("IFNULL(sum(cancel_pay),0) as cancelPayTotal,IFNULL(sum(pay_over_time),0) as payOverTimeTotal," +
                        "IFNULL(sum(confirm_over_time), 0) as confirmOverTimeTotal,IFNULl(sum(appeal_success), 0) as appealSuccessTotal," +
                        "IFNULL(sum(appeal_fail), 0) as appealFailTotal,IFNULl(sum(amount_error), 0) as amountErrorTotal," +
                        "IFNULL(sum(cancel), 0) as cancelOrderTotal,IFNULl(sum(success_order_num), 0) as successOrderNumTotal,IFNULL(sum(order_num),0) as orderNumTotal")
                .lambda();
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiPaymentOrder::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiPaymentOrder::getDateTime, req.getEndTime());
        }
        return baseMapper.selectOne(lambdaQuery);

    }


    @Override
    @SneakyThrows
    public RestResult<MemberOrderOverviewDTO> getMemberOrderOverview(CommonDateLimitReq req, RestResult<MemberOrderOverviewDTO> usdtData) {
        // 获取时间段内代收订单交易额、订单数
        CompletableFuture<MemberOrderOverviewDTO> memberPayAmountFuture = CompletableFuture.supplyAsync(() -> getPaymentOrderOverview(req));
        // 获取时间段内代付订单交易额、订单数
        CompletableFuture<MemberOrderOverviewDTO> memberWithdrawAmountFuture = CompletableFuture.supplyAsync(() -> iBiWithdrawOrderDailyService.getMemberOrderOverview(req));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(memberPayAmountFuture, memberWithdrawAmountFuture);
        allFutures.get();

        MemberOrderOverviewDTO payDto = memberPayAmountFuture.get();
        MemberOrderOverviewDTO withdrawDto = memberWithdrawAmountFuture.get();
        MemberOrderOverviewDTO usdtAmountDto = usdtData.getData();
        // 合并
        final CopyOptions copyOptions = CopyOptions.create();
        copyOptions.setIgnoreNullValue(true);
        BeanUtil.copyProperties(payDto, withdrawDto, copyOptions);
        BeanUtil.copyProperties(usdtAmountDto, withdrawDto, copyOptions);
        // 计算平均值和差异值
        BigDecimal memberPayAmount = withdrawDto.getMemberPayAmount();
        long memberPayTransNum = withdrawDto.getMemberPayTransNum();
        BigDecimal memberWithdrawAmount = withdrawDto.getMemberWithdrawAmount();
        long memberWithdrawTransNum = withdrawDto.getMemberWithdrawTransNum();
        BigDecimal usdtOrderAmount = withdrawDto.getUsdtOrderAmount();
        long usdtOrderNum = withdrawDto.getUsdtOrderNum();
        // 定义初始值
        BigDecimal avgPayAmount = BigDecimal.ZERO;
        BigDecimal avgWithdrawAmount = BigDecimal.ZERO;
        BigDecimal avgUsdtAmount = BigDecimal.ZERO;
        if (memberPayAmount.compareTo(BigDecimal.ZERO) != 0 && memberPayTransNum != 0) {
            avgPayAmount = memberPayAmount.divide(new BigDecimal(memberPayTransNum), 2, RoundingMode.HALF_UP);
        }

        if (memberWithdrawAmount.compareTo(BigDecimal.ZERO) != 0 && memberPayTransNum != 0) {
            avgWithdrawAmount = memberWithdrawAmount.divide(new BigDecimal(memberWithdrawTransNum), 2, RoundingMode.HALF_UP);
        }

        if (usdtOrderAmount.compareTo(BigDecimal.ZERO) != 0 && usdtOrderNum != 0) {
            avgUsdtAmount = usdtOrderAmount.divide(new BigDecimal(usdtOrderNum), 2, RoundingMode.HALF_UP);
        }
        BigDecimal diffTransAmount = memberPayAmount.subtract(memberWithdrawAmount);
        long diffTransNum = memberPayTransNum - memberWithdrawTransNum;
        BigDecimal diffAvgAmount = avgPayAmount.subtract(avgWithdrawAmount);
        // 整合数据
        withdrawDto.setPayAverageAmount(avgPayAmount);
        withdrawDto.setWithdrawAverageAmount(avgWithdrawAmount);
        withdrawDto.setUsdtAverageAmount(avgUsdtAmount);
        withdrawDto.setTransAmountDiff(diffTransAmount);
        withdrawDto.setTransNumDiff(diffTransNum);
        withdrawDto.setAverageAmountDiff(diffAvgAmount);
        return RestResult.ok(withdrawDto);
    }

    public MemberOrderOverviewDTO getPaymentOrderOverview(CommonDateLimitReq req) {
        LambdaQueryWrapper<BiPaymentOrder> lambdaQuery = new QueryWrapper<BiPaymentOrder>()
                .select("IFNULL(sum(actual_money), 0) as memberPayAmount," +
                        "        IFNULL(sum(success_order_num), 0) as memberPayTransNum")
                .lambda();
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiPaymentOrder::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiPaymentOrder::getDateTime, req.getEndTime());
        }
        BiPaymentOrder biPaymentOrder = baseMapper.selectOne(lambdaQuery);
        MemberOrderOverviewDTO result = new MemberOrderOverviewDTO();
        result.setMemberPayAmount(biPaymentOrder.getMemberPayAmount());
        result.setMemberPayTransNum(biPaymentOrder.getMemberPayTransNum());
        return result;
    }

}
