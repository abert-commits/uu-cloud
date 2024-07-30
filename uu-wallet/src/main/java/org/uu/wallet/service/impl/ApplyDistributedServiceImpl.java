package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.ApplyDistributedDTO;
import org.uu.common.pay.req.ApplyDistributedListPageReq;
import org.uu.wallet.Enum.AccountChangeEnum;
import org.uu.wallet.Enum.BalanceTypeEnum;
import org.uu.wallet.Enum.ChangeModeEnum;
import org.uu.wallet.Enum.DistributeddStatusEnum;
import org.uu.wallet.entity.ApplyDistributed;
import org.uu.wallet.mapper.ApplyDistributedMapper;
import org.uu.wallet.service.IApplyDistributedService;
import org.uu.wallet.util.AmountChangeUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApplyDistributedServiceImpl extends ServiceImpl<ApplyDistributedMapper, ApplyDistributed> implements IApplyDistributedService {
    private final AmountChangeUtil amountChangeUtil;

    @Override
    @SneakyThrows
    public PageReturn<ApplyDistributedDTO> listPage(ApplyDistributedListPageReq req) {
        Page<ApplyDistributed> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<ApplyDistributed> lambdaQuery = lambdaQuery();
        lambdaQuery.eq(ApplyDistributed::getStatus, DistributeddStatusEnum.NOFISHED.getCode());
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<ApplyDistributed> queryWrapper = new QueryWrapper<ApplyDistributed>()
                .select("IFNULL(sum(amount), 0) as amountTotal, IFNULL(sum(balance), 0) as balanceTotal").lambda();
        queryWrapper.eq(ApplyDistributed::getStatus, DistributeddStatusEnum.NOFISHED.getCode());

        if (!StringUtils.isEmpty(req.getCurrency())) {
            lambdaQuery.eq(ApplyDistributed::getCurrence, req.getCurrency());
            queryWrapper.eq(ApplyDistributed::getCurrence, req.getCurrency());
        }
        if (CollectionUtils.isNotEmpty(req.getMerchantCodes())) {
            lambdaQuery.in(ApplyDistributed::getMerchantCode, req.getMerchantCodes());
            queryWrapper.in(ApplyDistributed::getMerchantCode, req.getMerchantCodes());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUsername())) {
            lambdaQuery.eq(ApplyDistributed::getUsername, req.getUsername());
            queryWrapper.eq(ApplyDistributed::getUsername, req.getUsername());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getOrderNo())) {
            lambdaQuery.eq(ApplyDistributed::getOrderNo, req.getOrderNo());
            queryWrapper.eq(ApplyDistributed::getOrderNo, req.getOrderNo());
        }

        if (!ObjectUtils.isEmpty(req.getStartTime())) {
            lambdaQuery.ge(ApplyDistributed::getCreateTime, req.getStartTime());
            queryWrapper.ge(ApplyDistributed::getCreateTime, req.getStartTime());
        }

        //--动态查询 结束时间
        if (!ObjectUtils.isEmpty(req.getEndTime())) {
            lambdaQuery.le(ApplyDistributed::getCreateTime, req.getEndTime());
            queryWrapper.le(ApplyDistributed::getCreateTime, req.getEndTime());
        }
        lambdaQuery.orderByDesc(ApplyDistributed::getId);
        Page<ApplyDistributed> finalPage = page;
        CompletableFuture<ApplyDistributed> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<ApplyDistributed>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        ApplyDistributed totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal().toPlainString());
        extent.put("balanceTotal", totalInfo.getBalanceTotal().toPlainString());
        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal balancePageTotal = BigDecimal.ZERO;
        List<ApplyDistributedDTO> accountChangeVos = new ArrayList<>();
        List<ApplyDistributed> records = page.getRecords();
        for (ApplyDistributed record : records) {
            ApplyDistributedDTO dto = new ApplyDistributedDTO();
            BeanUtils.copyProperties(record, dto);
            accountChangeVos.add(dto);
            amountPageTotal = amountPageTotal.add(record.getAmount());
            balancePageTotal = balancePageTotal.add(record.getBalance());
        }
        extent.put("amountPageTotal", amountPageTotal.toPlainString());
        extent.put("balancePageTotal", balancePageTotal.toPlainString());

        return PageUtils.flush(page, accountChangeVos, extent);
    }


    @Override
    public ApplyDistributedDTO listRecordTotal(ApplyDistributedListPageReq req) {


        QueryWrapper<ApplyDistributed> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "sum(balance) as balance",
                "sum(amount) as amount"

        );

        if (!StringUtils.isEmpty(req.getUsername())) {
            queryWrapper.eq("username", req.getUsername());
        }

        if (!StringUtils.isEmpty(req.getOrderNo())) {
            queryWrapper.eq("order_no", req.getOrderNo());
        }
        queryWrapper.eq("status", DistributeddStatusEnum.FINISHED.getCode());


        if (!StringUtils.isEmpty(req.getStartTime())) {
            queryWrapper.ge("create_time", req.getStartTime());
        }
        if (!StringUtils.isEmpty(req.getEndTime())) {
            queryWrapper.le("create_time", req.getEndTime());
        }

        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page, queryWrapper);
        List<Map<String, Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<ApplyDistributedDTO> list = jsonArray.toJavaList(ApplyDistributedDTO.class);
        ApplyDistributedDTO matchingOrderDTO = list.get(0);

        return matchingOrderDTO;
    }


    @Override
    @SneakyThrows
    public PageReturn<ApplyDistributedDTO> listRecordPage(ApplyDistributedListPageReq req) {
        Page<ApplyDistributed> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<ApplyDistributed> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(ApplyDistributed::getCreateTime);
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<ApplyDistributed> queryWrapper = new QueryWrapper<ApplyDistributed>()
                .select("IFNULL(sum(amount), 0) as amountTotal, IFNULL(sum(balance), 0) as balanceTotal").lambda();

        if (!StringUtils.isEmpty(req.getCurrency())) {
            lambdaQuery.eq(ApplyDistributed::getCurrence, req.getCurrency());
            queryWrapper.eq(ApplyDistributed::getCurrence, req.getCurrency());
        }
        if (!StringUtils.isEmpty(req.getUsername())) {
            lambdaQuery.eq(ApplyDistributed::getUsername, req.getUsername());
            queryWrapper.eq(ApplyDistributed::getUsername, req.getUsername());
        }
        if (!StringUtils.isEmpty(req.getMerchantCode())) {
            lambdaQuery.eq(ApplyDistributed::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(ApplyDistributed::getMerchantCode, req.getMerchantCode());
        }
        if (CollectionUtils.isNotEmpty(req.getMerchantCodes())) {
            lambdaQuery.in(ApplyDistributed::getMerchantCode, req.getMerchantCodes());
            queryWrapper.in(ApplyDistributed::getMerchantCode, req.getMerchantCodes());
        }

        if (!StringUtils.isEmpty(req.getOrderNo())) {
            lambdaQuery.eq(ApplyDistributed::getOrderNo, req.getOrderNo());
            queryWrapper.eq(ApplyDistributed::getOrderNo, req.getOrderNo());
        }

        if (req.getStartTime() != null) {
            lambdaQuery.ge(ApplyDistributed::getCreateTime, req.getStartTime());
            queryWrapper.ge(ApplyDistributed::getCreateTime, req.getStartTime());
        }

        //--动态查询 结束时间
        if (req.getEndTime() != null) {
            lambdaQuery.le(ApplyDistributed::getCreateTime, req.getEndTime());
            queryWrapper.le(ApplyDistributed::getCreateTime, req.getEndTime());
        }

        Page<ApplyDistributed> finalPage = page;
        CompletableFuture<ApplyDistributed> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<ApplyDistributed>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        ApplyDistributed totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal().toPlainString());
        extent.put("balanceTotal", totalInfo.getBalanceTotal().toPlainString());
        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal balancePageTotal = BigDecimal.ZERO;
        List<ApplyDistributedDTO> accountChangeVos = new ArrayList<>();
        List<ApplyDistributed> records = page.getRecords();
        for (ApplyDistributed record : records) {
            ApplyDistributedDTO dto = new ApplyDistributedDTO();
            BeanUtils.copyProperties(record, dto);
            accountChangeVos.add(dto);
            amountPageTotal = amountPageTotal.add(record.getAmount());
            balancePageTotal = balancePageTotal.add(record.getBalance());
        }
        extent.put("amountPageTotal", amountPageTotal.toPlainString());
        extent.put("balancePageTotal", balancePageTotal.toPlainString());
        return PageUtils.flush(page, accountChangeVos, extent);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplyDistributedDTO distributed(ApplyDistributed applyDistributed) {
        applyDistributed.setStatus(DistributeddStatusEnum.FINISHED.getCode());
        baseMapper.updateById(applyDistributed);
        ApplyDistributedDTO applyDistributedDTO = new ApplyDistributedDTO();
        amountChangeUtil.insertOrUpdateAccountChange(applyDistributed.getMerchantCode(), applyDistributed.getAmount(), ChangeModeEnum.SUB, applyDistributed.getCurrence(),
                applyDistributed.getOrderNo(), AccountChangeEnum.WITHDRAW, applyDistributed.getRemark(), "", "", "", BalanceTypeEnum.getNameByCode(applyDistributed.getPayType()));
        BeanUtils.copyProperties(applyDistributed, applyDistributedDTO);
        return applyDistributedDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplyDistributedDTO noDistributed(ApplyDistributed applyDistributed) {
        applyDistributed.setStatus(DistributeddStatusEnum.NODISTRIBUTED.getCode());
        baseMapper.updateById(applyDistributed);
        ApplyDistributedDTO applyDistributedDTO = new ApplyDistributedDTO();
        amountChangeUtil.updateMerchantBalance(applyDistributed.getMerchantCode(), applyDistributed.getAmount(), ChangeModeEnum.ADD,
                applyDistributed.getOrderNo(), AccountChangeEnum.WITHDRAW_BACK, BalanceTypeEnum.getNameByCode(applyDistributed.getPayType()));
        BeanUtils.copyProperties(applyDistributed, applyDistributedDTO);
        return applyDistributedDTO;

    }

    @Override
    public Map<String, List<ApplyDistributed>> applyDistributedMap(String merchantCode) {
        return lambdaQuery()
                .eq(ApplyDistributed::getStatus, DistributeddStatusEnum.FINISHED.getCode())
                .eq(ApplyDistributed::getMerchantCode, merchantCode)
                .list()
                .parallelStream()
                .collect(Collectors.groupingByConcurrent(ApplyDistributed::getPayType));
    }


}
