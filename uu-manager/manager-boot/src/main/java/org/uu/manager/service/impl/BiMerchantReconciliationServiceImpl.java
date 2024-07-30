package org.uu.manager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiMerchantReconciliationDTO;
import org.uu.manager.entity.BiMerchantReconciliation;
import org.uu.manager.mapper.BiMerchantReconciliationMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMerchantReconciliationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 商户对账报表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
@Service
public class BiMerchantReconciliationServiceImpl extends ServiceImpl<BiMerchantReconciliationMapper, BiMerchantReconciliation> implements IBiMerchantReconciliationService {

    @SneakyThrows
    @Override
    public PageReturn<BiMerchantReconciliationDTO> listPage(MerchantDailyReportReq req) {
        BigDecimal balancePageTotal = new BigDecimal(0);
        BigDecimal payMoneyPageTotal = new BigDecimal(0);
        BigDecimal withdrawMoneyPageTotal = new BigDecimal(0);
        BigDecimal payFeePageTotal = new BigDecimal(0);
        BigDecimal withdrawFeePageTotal = new BigDecimal(0);
        BigDecimal merchantUpPageTotal = new BigDecimal(0);
        BigDecimal merchantDownPageTotal = new BigDecimal(0);
        BigDecimal merchantDiffPageTotal = new BigDecimal(0);
        Page<BiMerchantReconciliation> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryWrapper<BiMerchantReconciliation> queryWrapper = new QueryWrapper<BiMerchantReconciliation>()
                .select("IFNULL(sum(merchant_balance), 0) as balanceTotal,\n" +
                        "       IFNULL(sum(pay_money), 0)        as payMoneyTotal,\n" +
                        "       IFNULL(sum(withdraw_money), 0)   as withdrawMoneyTotal,\n" +
                        "       IFNULL(sum(pay_fee), 0)          as payFeeTotal,\n" +
                        "       IFNULL(sum(withdraw_fee), 0)     as withdrawFeeTotal,\n" +
                        "       IFNULL(sum(merchant_up), 0)      as merchantUpTotal,\n" +
                        "       IFNULL(sum(merchant_down), 0)    as merchantDownTotal,\n" +
                        "       IFNULL(sum(merchant_diff), 0)    as merchantDiffTotal").lambda();
        LambdaQueryChainWrapper<BiMerchantReconciliation> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMerchantReconciliation::getDateTime);
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMerchantReconciliation::getDateTime, req.getStartTime());
            queryWrapper.ge(BiMerchantReconciliation::getDateTime, req.getStartTime());
        }
        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMerchantReconciliation::getDateTime, req.getEndTime());
            queryWrapper.le(BiMerchantReconciliation::getDateTime, req.getEndTime());
        }
        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiMerchantReconciliation::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(BiMerchantReconciliation::getMerchantCode, req.getMerchantCode());
        }
        CompletableFuture<BiMerchantReconciliation> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        Page<BiMerchantReconciliation> finalPage = page;
        CompletableFuture<Page<BiMerchantReconciliation>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(totalFuture, resultFuture);
        allFutures.get();
        page = resultFuture.get();
        BiMerchantReconciliation totalData = totalFuture.get();
        JSONObject extend = new JSONObject();
        extend.put("balanceTotal", totalData.getBalanceTotal());
        extend.put("payMoneyTotal", totalData.getPayMoneyTotal());
        extend.put("withdrawMoneyTotal", totalData.getWithdrawMoneyTotal());
        extend.put("payFeeTotal", totalData.getPayFeeTotal());
        extend.put("withdrawFeeTotal", totalData.getWithdrawFeeTotal());
        extend.put("merchantUpTotal", totalData.getMerchantUpTotal());
        extend.put("merchantDownTotal", totalData.getMerchantDownTotal());
        extend.put("merchantDiffTotal", totalData.getMerchantDiffTotal());
        List<BiMerchantReconciliation> records = page.getRecords();
        List<BiMerchantReconciliationDTO> list = new ArrayList<>();
        for (BiMerchantReconciliation record : records) {
            balancePageTotal = balancePageTotal.add(new BigDecimal(record.getMerchantBalance().toString()));
            payMoneyPageTotal = payMoneyPageTotal.add(new BigDecimal(record.getPayMoney().toString()));
            withdrawMoneyPageTotal = withdrawMoneyPageTotal.add(new BigDecimal(record.getWithdrawMoney().toString()));
            payFeePageTotal = payFeePageTotal.add(new BigDecimal(record.getPayFee().toString()));
            withdrawFeePageTotal = withdrawFeePageTotal.add(new BigDecimal(record.getWithdrawFee().toString()));
            merchantUpPageTotal = merchantUpPageTotal.add(new BigDecimal(record.getMerchantUp().toString()));
            merchantDownPageTotal = merchantDownPageTotal.add(new BigDecimal(record.getMerchantDown().toString()));
            merchantDiffPageTotal = merchantDiffPageTotal.add(new BigDecimal(record.getMerchantDiff().toString()));
            BiMerchantReconciliationDTO data = new BiMerchantReconciliationDTO();
            BeanUtils.copyProperties(record, data);
            list.add(data);
        }
        extend.put("balancePageTotal", balancePageTotal);
        extend.put("payMoneyPageTotal", payMoneyPageTotal);
        extend.put("withdrawMoneyPageTotal", withdrawMoneyPageTotal);
        extend.put("payFeePageTotal", payFeePageTotal);
        extend.put("withdrawFeePageTotal", withdrawFeePageTotal);
        extend.put("merchantUpPageTotal", merchantUpPageTotal);
        extend.put("merchantDownPageTotal", merchantDownPageTotal);
        extend.put("merchantDiffPageTotal", merchantDiffPageTotal);
        return PageUtils.flush(page, list, extend);
    }
}
