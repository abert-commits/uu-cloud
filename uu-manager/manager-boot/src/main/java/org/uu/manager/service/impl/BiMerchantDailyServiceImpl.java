package org.uu.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiMerchantDailyDTO;
import org.uu.common.pay.dto.BiPaymentOrderDTO;
import org.uu.common.web.exception.BizException;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiPaymentOrderMonth;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.mapper.BiMerchantDailyMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMerchantDailyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author
 */
@Service
public class BiMerchantDailyServiceImpl extends ServiceImpl<BiMerchantDailyMapper, BiMerchantDaily> implements IBiMerchantDailyService {

    @Override
    @SneakyThrows
    public PageReturn<BiMerchantDaily> listPage(MerchantDailyReportReq req) {
        Page<BiMerchantDaily> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiMerchantDaily> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMerchantDaily::getDateTime);
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<BiMerchantDaily> queryWrapper = new QueryWrapper<BiMerchantDaily>()
                .select("IFNULL(sum(collection_initiation_amount) , 0) as collectionInitiationAmountTotal," +
                        "IFNUll(sum(pay_order_num) , 0) as payOrderNumTotal," +
                        "IFNUll(sum(pay_success_order_num) , 0) as paySuccessOrderNumTotal," +
                        "IFNUll(sum(pay_money) , 0) as payMoneyTotal," +
                        "IFNUll(sum(payment_initiation_amount) , 0) as paymentInitiationAmountTotal," +
                        "IFNUll(sum(withdraw_order_num) , 0) as withdrawOrderNumTotal," +
                        "IFNUll(sum(withdraw_success_order_num) , 0) as withdrawSuccessOrderNumTotal," +
                        "IFNUll(sum(withdraw_money) , 0) as withdrawMoneyTotal," +
                        "IFNUll(sum(difference) , 0) as differenceTotal," +
                        "IFNUll(sum(collection_fee) , 0) as collectionFeeTotal," +
                        "ifnull(sum(payment_fee) , 0) as_payment_fee_total"

                ).lambda();

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMerchantDaily::getDateTime, req.getStartTime());
            queryWrapper.ge(BiMerchantDaily::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMerchantDaily::getDateTime, req.getEndTime());
            queryWrapper.le(BiMerchantDaily::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiMerchantDaily::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(BiMerchantDaily::getMerchantCode, req.getMerchantCode());
        }
        Page<BiMerchantDaily> finalPage = page;
        CompletableFuture<BiMerchantDaily> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<BiMerchantDaily>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        BiMerchantDaily totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("collectionInitiationAmountTotal", totalInfo.getCollectionInitiationAmountTotal().toPlainString());
        extent.put("payOrderNumTotal", totalInfo.getPayOrderNumTotal());
        extent.put("paySuccessOrderNumTotal", totalInfo.getPaySuccessOrderNumTotal());
        extent.put("payMoneyTotal", totalInfo.getPayMoneyTotal().toPlainString());
        extent.put("paymentInitiationAmountTotal", totalInfo.getPaymentInitiationAmountTotal().toPlainString());
        extent.put("withdrawOrderNumTotal", totalInfo.getWithdrawOrderNumTotal());
        extent.put("withdrawSuccessOrderNumTotal", totalInfo.getWithdrawSuccessOrderNumTotal());
        extent.put("withdrawMoneyTotal", totalInfo.getWithdrawMoneyTotal().toPlainString());
        extent.put("differenceTotal", totalInfo.getDifferenceTotal().toPlainString());
        extent.put("collectionFeeTotal", totalInfo.getCollectionFeeTotal().toPlainString());
        extent.put("paymentFeeTotal", totalInfo.getPaymentFeeTotal().toPlainString());

        BigDecimal collectionInitiationAmountPageTotal = BigDecimal.ZERO;
        BigDecimal payMoneyPageTotal = BigDecimal.ZERO;
        BigDecimal paymentInitiationAmountPageTotal = BigDecimal.ZERO;
        BigDecimal withdrawMoneyPageTotal = BigDecimal.ZERO;
        BigDecimal differencePageTotal = BigDecimal.ZERO;
        BigDecimal collectionFeePageTotal = BigDecimal.ZERO;
        BigDecimal paymentFeePageTotal = BigDecimal.ZERO;
        long payOrderNumPageTotal = 0L;
        long paySuccessOrderNumPageTotal = 0L;
        long withdrawOrderNumPageTotal = 0L;
        long withdrawSuccessOrderNumPageTotal = 0L;

        List<BiMerchantDaily> records = page.getRecords();
        for (BiMerchantDaily item : records) {
            item.setDifference(item.getWithdrawMoney().subtract(item.getPayMoney()));
            if (item.getPayOrderNum() <= 0L) {
                item.setPaySuccessRate(0d);
            } else {
                double payRate = new BigDecimal(item.getPaySuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getPayOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                item.setPaySuccessRate(payRate);

            }

            if (item.getWithdrawOrderNum() <= 0L) {
                item.setWithdrawSuccessRate(0d);
            } else {
                double withdrawRate = new BigDecimal(item.getWithdrawSuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getWithdrawOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                item.setWithdrawSuccessRate(withdrawRate);
            }
            collectionInitiationAmountPageTotal = collectionInitiationAmountPageTotal.add(item.getCollectionInitiationAmount());
            payMoneyPageTotal = payMoneyPageTotal.add(item.getPayMoney());
            paymentInitiationAmountPageTotal = paymentInitiationAmountPageTotal.add(item.getPaymentInitiationAmount());
            withdrawMoneyPageTotal = withdrawMoneyPageTotal.add(item.getWithdrawMoney());
            differencePageTotal = differencePageTotal.add(item.getDifference());
            collectionFeePageTotal = collectionFeePageTotal.add(item.getCollectionFee());
            paymentFeePageTotal = paymentFeePageTotal.add(item.getPaymentFee());
            payOrderNumPageTotal = payOrderNumPageTotal + item.getPayOrderNum();
            paySuccessOrderNumPageTotal = paySuccessOrderNumPageTotal + item.getPaySuccessOrderNum();
            withdrawOrderNumPageTotal = withdrawOrderNumPageTotal + item.getWithdrawOrderNum();
            withdrawSuccessOrderNumPageTotal = withdrawSuccessOrderNumPageTotal + item.getWithdrawSuccessOrderNum();
        }
        extent.put("collectionInitiationAmountPageTotal", collectionInitiationAmountPageTotal.toPlainString());
        extent.put("payMoneyPageTotal", payMoneyPageTotal.toPlainString());
        extent.put("paymentInitiationAmountPageTotal", paymentInitiationAmountPageTotal.toPlainString());
        extent.put("withdrawMoneyPageTotal", withdrawMoneyPageTotal.toPlainString());
        extent.put("differencePageTotal", differencePageTotal.toPlainString());
        extent.put("collectionFeePageTotal", collectionFeePageTotal.toPlainString());
        extent.put("paymentFeePageTotal", paymentFeePageTotal.toPlainString());
        extent.put("payOrderNumPageTotal", payOrderNumPageTotal);
        extent.put("paySuccessOrderNumPageTotal", paySuccessOrderNumPageTotal);
        extent.put("withdrawOrderNumPageTotal", withdrawOrderNumPageTotal);
        extent.put("withdrawSuccessOrderNumPageTotal", withdrawSuccessOrderNumPageTotal);
        return PageUtils.flush(page, records, extent);
    }

    @Override
    public PageReturn<BiMerchantDailyDTO> listPageForExport(MerchantDailyReportReq req) {
        List<BiMerchantDailyDTO> list = new ArrayList<>();
        PageReturn<BiMerchantDaily> biPaymentOrderPageReturn = listPage(req);
        List<BiMerchantDaily> data = biPaymentOrderPageReturn.getList();
        for (BiMerchantDaily item : data) {
            BiMerchantDailyDTO biMerchantDailyDTO = new BiMerchantDailyDTO();
            BeanUtils.copyProperties(item, biMerchantDailyDTO);
            String type = "内部商户";
            if (biMerchantDailyDTO.getMerchantType().equals("2")) {
                type = "外部商户";
            }
            biMerchantDailyDTO.setMerchantType(type);

            biMerchantDailyDTO.setPayMoney(item.getPayMoney().toString());
            biMerchantDailyDTO.setWithdrawMoney(item.getWithdrawMoney().toString());
            if (item.getPaySuccessRate() != null) {
                double successRateD = (item.getPaySuccessRate() * 100);
                String paySuccessRateStr = (int) successRateD + "%";
                biMerchantDailyDTO.setPaySuccessRate(paySuccessRateStr);
            }
            if (item.getWithdrawSuccessRate() != null) {
                double successRateW = (item.getWithdrawSuccessRate() * 100);
                String withdrawSuccessRateStr = (int) successRateW + "%";
                biMerchantDailyDTO.setWithdrawSuccessRate(withdrawSuccessRateStr);
            }
            biMerchantDailyDTO.setDifference(item.getDifference().toString());
            biMerchantDailyDTO.setTotalFee(item.getTotalFee().toString());

            list.add(biMerchantDailyDTO);
        }
        Page<BiMerchantDailyDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(biPaymentOrderPageReturn.getTotal());
        return PageUtils.flush(page, list);
    }
}
