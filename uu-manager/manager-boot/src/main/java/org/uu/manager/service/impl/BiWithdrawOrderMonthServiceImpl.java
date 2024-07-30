package org.uu.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.DurationCalculatorUtil;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiWithdrawOrderDailyExportDTO;
import org.uu.common.web.exception.BizException;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.entity.BiWithdrawOrderMonth;
import org.uu.manager.mapper.BiWithdrawOrderMonthMapper;
import org.uu.manager.req.WithdrawDailyOrderReportReq;
import org.uu.manager.req.WithdrawMonthOrderReportReq;
import org.uu.manager.service.IBiWithdrawOrderMonthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author
 */
@Service
public class BiWithdrawOrderMonthServiceImpl extends ServiceImpl<BiWithdrawOrderMonthMapper, BiWithdrawOrderMonth> implements IBiWithdrawOrderMonthService {

    @Override
    @SneakyThrows
    public PageReturn<BiWithdrawOrderMonth> listPage(WithdrawMonthOrderReportReq req) {
        Page<BiWithdrawOrderMonth> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiWithdrawOrderMonth> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiWithdrawOrderMonth::getDateTime);

        // 新增统计金额字段总计字段
        LambdaQueryWrapper<BiWithdrawOrderMonth> queryWrapper = new QueryWrapper<BiWithdrawOrderMonth>()
                .select("IFNULL(sum(actual_money), 0) as actualMoneyTotal, IFNULL(sum(total_fee), 0) as feeTotal").lambda();

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiWithdrawOrderMonth::getDateTime, req.getStartTime());
            queryWrapper.ge(BiWithdrawOrderMonth::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiWithdrawOrderMonth::getDateTime, req.getEndTime());
            queryWrapper.le(BiWithdrawOrderMonth::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiWithdrawOrderMonth::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(BiWithdrawOrderMonth::getMerchantCode, req.getMerchantCode());
        }
        Page<BiWithdrawOrderMonth> finalPage = page;
        CompletableFuture<BiWithdrawOrderMonth> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<BiWithdrawOrderMonth>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        BiWithdrawOrderMonth totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("actualMoneyTotal", totalInfo.getActualMoneyTotal().toPlainString());
        extent.put("feeTotal", totalInfo.getFeeTotal().toPlainString());
        BigDecimal actualMoneyPageTotal = BigDecimal.ZERO;
        BigDecimal feePageTotal = BigDecimal.ZERO;

        List<BiWithdrawOrderMonth> records = page.getRecords();
        for (BiWithdrawOrderMonth item : records) {
            if(item.getOrderNum() <= 0L){
                item.setSuccessRate(0d);
                item.setAverageFinishDuration(0L);
                item.setAverageMatchDuration(0L);
            }else {
                double result = new BigDecimal(item.getSuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                item.setSuccessRate(result);
                Long  averageFinishDuration = new BigDecimal(item.getFinishDuration()).
                        divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).longValue();
                item.setAverageFinishDuration(averageFinishDuration);
                Long  averageMatchDuration = new BigDecimal(item.getMatchDuration()).
                        divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).longValue();
                item.setAverageMatchDuration(averageMatchDuration);
            }
            actualMoneyPageTotal = actualMoneyPageTotal.add(item.getActualMoney());
            feePageTotal = feePageTotal.add(item.getTotalFee());
        }
        extent.put("actualMoneyPageTotal",actualMoneyPageTotal.toPlainString());
        extent.put("feePageTotal", feePageTotal.toPlainString());
        return PageUtils.flush(page, records, extent);
    }

    @Override
    public PageReturn<BiWithdrawOrderDailyExportDTO> listPageForExport(WithdrawMonthOrderReportReq req) {
        List<BiWithdrawOrderDailyExportDTO> list = new ArrayList<>();
        PageReturn<BiWithdrawOrderMonth> biWithdrawOrderDailyPageReturn = listPage(req);
        List<BiWithdrawOrderMonth> data = biWithdrawOrderDailyPageReturn.getList();
        for (BiWithdrawOrderMonth item : data) {
            BiWithdrawOrderDailyExportDTO biWithdrawOrderDailyExportDTO = new BiWithdrawOrderDailyExportDTO();
            BeanUtils.copyProperties(item, biWithdrawOrderDailyExportDTO);
            if(item.getSuccessRate() != null){
                double successRateD = (item.getSuccessRate() * 100);
                String successRateStr = (int) successRateD + "%";
                biWithdrawOrderDailyExportDTO.setSuccessRate(successRateStr);
            }
            String orderCompleteDuration = DurationCalculatorUtil.getOrderCompleteDuration(item.getAverageFinishDuration().toString());
            biWithdrawOrderDailyExportDTO.setAverageFinishDuration(orderCompleteDuration);
            biWithdrawOrderDailyExportDTO.setActualMoney(item.getActualMoney().toString());
            biWithdrawOrderDailyExportDTO.setTotalFee(item.getTotalFee().toString());
            list.add(biWithdrawOrderDailyExportDTO);
        }

        Page<BiWithdrawOrderDailyExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(biWithdrawOrderDailyPageReturn.getTotal());
        return PageUtils.flush(page, list);
    }
}
