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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MatchPoolListPageDTO;
import org.uu.common.pay.dto.PaymentOrderChildDTO;
import org.uu.common.pay.req.MatchPoolGetChildReq;
import org.uu.common.pay.req.MatchPoolListPageReq;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.MatchPool;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.MatchPoolMapper;
import org.uu.wallet.service.IMatchPoolService;
import org.uu.wallet.service.IPaymentOrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchPoolServiceImpl extends ServiceImpl<MatchPoolMapper, MatchPool> implements IMatchPoolService {

    @Autowired
    private IPaymentOrderService paymentOrderService;

    /**
     * 根据订单号获取订单信息
     *
     * @param orderNo
     * @return {@link MatchPool}
     */
    @Override
    public MatchPool getMatchPoolOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(MatchPool::getMatchOrder, orderNo).one();
    }


    @Override
    @SneakyThrows
    public PageReturn<MatchPoolListPageDTO> listPage(MatchPoolListPageReq req) {
        Page<MatchPool> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MatchPool> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(MatchPool::getCreateTime);
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MatchPool> queryWrapper = new QueryWrapper<MatchPool>()
                .select("IFNULL(sum(amount),0) as amountTotal,IFNULL(sum(sold_amount), 0) as soldAmountTotal,IFNULL(sum(remaining_amount),0) as sumRemainingAmount").lambda();

        if (StringUtils.isNotBlank(req.getMatchOrder())) {
            lambdaQuery.eq(MatchPool::getMatchOrder, req.getMatchOrder());
            queryWrapper.eq(MatchPool::getMatchOrder, req.getMatchOrder());
        }

        if (StringUtils.isNotBlank(req.getOrderStatus())) {
            lambdaQuery.eq(MatchPool::getOrderStatus, req.getOrderStatus());
            queryWrapper.eq(MatchPool::getOrderStatus, req.getOrderStatus());
        }


        if (!ObjectUtils.isEmpty(req.getCreateTimeStart())) {
            lambdaQuery.ge(MatchPool::getCreateTime, req.getCreateTimeStart());
            queryWrapper.ge(MatchPool::getCreateTime, req.getCreateTimeStart());
        }

        if (!ObjectUtils.isEmpty(req.getCreateTimeEnd())) {
            lambdaQuery.le(MatchPool::getCreateTime, req.getCreateTimeEnd());
            queryWrapper.le(MatchPool::getCreateTime, req.getCreateTimeEnd());
        }

        if (!ObjectUtils.isEmpty(req.getAmountStart())) {
            lambdaQuery.ge(MatchPool::getAmount, req.getAmountStart());
            queryWrapper.ge(MatchPool::getAmount, req.getAmountStart());
        }

        if (!ObjectUtils.isEmpty(req.getAmountEnd())) {
            lambdaQuery.le(MatchPool::getAmount, req.getAmountEnd());
            queryWrapper.le(MatchPool::getAmount, req.getAmountEnd());
        }

        if (!ObjectUtils.isEmpty(req.getMinimumAmountStart())) {
            lambdaQuery.ge(MatchPool::getMinimumAmount, req.getMinimumAmountStart());
            queryWrapper.ge(MatchPool::getMinimumAmount, req.getMinimumAmountStart());
        }

        if (!ObjectUtils.isEmpty(req.getMinimumAmountEnd())) {
            lambdaQuery.le(MatchPool::getMinimumAmount, req.getMinimumAmountEnd());
            queryWrapper.le(MatchPool::getMinimumAmount, req.getMinimumAmountEnd());
        }
        Page<MatchPool> finalPage = page;
        CompletableFuture<MatchPool> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<MatchPool>> matchPoolFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));

        CompletableFuture.allOf(totalFuture, matchPoolFuture);

        page = matchPoolFuture.get();
        MatchPool totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("sumRemainingAmount", totalInfo.getSumRemainingAmount());
        extent.put("amountTotal", totalInfo.getAmountTotal());
        extent.put("soldAmountTotal", totalInfo.getSoldAmountTotal());
        List<MatchPool> records = page.getRecords();
        BigDecimal remainingAmountPageTotal = new BigDecimal(0);
        BigDecimal amountPageTotal = new BigDecimal(0);
        BigDecimal soldAmountPageTotal = new BigDecimal(0);
        List<MatchPoolListPageDTO> listDto = new ArrayList<MatchPoolListPageDTO>();
        for (MatchPool matchPool : records) {
            remainingAmountPageTotal = remainingAmountPageTotal.add(matchPool.getRemainingAmount());
            amountPageTotal = amountPageTotal.add(matchPool.getAmount());
            soldAmountPageTotal = soldAmountPageTotal.add(matchPool.getSoldAmount());
            MatchPoolListPageDTO matchPoolListPageDTO = new MatchPoolListPageDTO();
            BeanUtils.copyProperties(matchPool, matchPoolListPageDTO);
            listDto.add(matchPoolListPageDTO);
        }
        extent.put("remainingAmountPageTotal", remainingAmountPageTotal);
        extent.put("amountPageTotal", amountPageTotal);
        extent.put("soldAmountPageTotal", soldAmountPageTotal);
        // List<MatchPoolDTO> listDTO = walletMapStruct.matchPoolTransform(records);
        return PageUtils.flush(page, listDto, extent);
    }

    @Override
    public MatchPoolListPageDTO matchPooTotal(MatchPoolListPageReq req) {
        QueryWrapper<MatchPool> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "sum(amount) as amount"
        );
        if (req.getCreateTimeStart()!=null) {
            queryWrapper.ge("create_time", req.getCreateTimeStart());
        }
        if (req.getCreateTimeEnd()!=null) {
            queryWrapper.le("create_time", req.getCreateTimeEnd());
        }
        if (!StringUtils.isBlank(req.getOrderStatus())) {
            queryWrapper.eq("order_status", req.getOrderStatus());
        }
        if (!StringUtils.isBlank(req.getMatchOrder())) {
            queryWrapper.eq("match_order", req.getMatchOrder());
        }
        if (req.getMinimumAmountStart() != null) {
            queryWrapper.ge("mininum_amount", req.getMinimumAmountStart());
        }
        if (req.getMinimumAmountEnd() != null) {
            queryWrapper.le("mininum_amount", req.getMinimumAmountEnd());
        }
        if (req.getAmountStart() != null) {
            queryWrapper.le("amount", req.getAmountStart());
        }
        if (req.getAmountEnd() != null) {
            queryWrapper.le("amount", req.getAmountEnd());
        }

        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page, queryWrapper);
        List<Map<String, Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<MatchPoolListPageDTO> list = jsonArray.toJavaList(MatchPoolListPageDTO.class);
        MatchPoolListPageDTO matchPoolDTO = list.get(0);

        return matchPoolDTO;

    }

    @Override
    public List<PaymentOrderChildDTO> getChildren(MatchPoolGetChildReq req) {

        List<PaymentOrder> list = paymentOrderService.lambdaQuery().eq(PaymentOrder::getMatchOrder, req.getMatchOrder()).list();
        //List<PaymentOrderDTO> listDto = walletMapStruct.paymentOrderTransform(list);
        List<PaymentOrderChildDTO> listDto = new ArrayList<PaymentOrderChildDTO>();
        for (PaymentOrder paymentOrder : list) {
            PaymentOrderChildDTO paymentOrderChildDTO = new PaymentOrderChildDTO();
            BeanUtils.copyProperties(paymentOrder, paymentOrderChildDTO);
            listDto.add(paymentOrderChildDTO);
        }
        return listDto;
    }
}
