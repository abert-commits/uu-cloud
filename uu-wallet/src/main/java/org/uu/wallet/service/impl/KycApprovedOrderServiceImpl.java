package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.KycApprovedOrderDTO;
import org.uu.common.pay.req.KycApprovedOrderListPageReq;
import org.uu.wallet.entity.KycApprovedOrder;
import org.uu.wallet.mapper.KycApprovedOrderMapper;
import org.uu.wallet.service.IKycApprovedOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 通过 KYC 验证完成的订单表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-05-03
 */
@Service
public class KycApprovedOrderServiceImpl extends ServiceImpl<KycApprovedOrderMapper, KycApprovedOrder> implements IKycApprovedOrderService {

    /**
     * 根据卖出订单号查询KYC交易订单是否存在
     *
     * @return boolean 如果 KYC交易订单存在，返回true；否则返回false。
     */
    @Override
    public Boolean checkKycTransactionExistsBySellOrderId(String sellerOrderId) {

        int count = lambdaQuery()
                .eq(KycApprovedOrder::getSellerOrderId, sellerOrderId) // 卖出订单号
                .count();

        // 如果记录数量大于0，则表示该订单已存在
        return count > 0;
    }

    @Override
    public Boolean checkKycTransactionExistsByUtr(String utr) {

        int count = lambdaQuery()
                .eq(KycApprovedOrder::getUtr, utr)
                .count();
        return count > 0;
    }

    @Override
    @SneakyThrows
    public PageReturn<KycApprovedOrderDTO> listPage(KycApprovedOrderListPageReq req) {
        Page<KycApprovedOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        // 新增统计金额字段总计字段
        LambdaQueryWrapper<KycApprovedOrder> queryWrapper = new QueryWrapper<KycApprovedOrder>()
                .select("IFNULL(sum(amount),0) as amountTotal").lambda();

        LambdaQueryChainWrapper<KycApprovedOrder> lambdaQuery = lambdaQuery();
        if (ObjectUtils.isNotEmpty(req.getStartTime())) {
            lambdaQuery.ge(KycApprovedOrder::getCreateTime, req.getStartTime());
        }
        if (ObjectUtils.isNotEmpty(req.getStartTime())) {
            lambdaQuery.le(KycApprovedOrder::getCreateTime, req.getEndTime());
        }
        if (ObjectUtils.isNotEmpty(req.getBuyerOrderId())) {
            lambdaQuery.eq(KycApprovedOrder::getBuyerOrderId, req.getBuyerOrderId());
        }
        if (ObjectUtils.isNotEmpty(req.getSellerOrderId())) {
            lambdaQuery.eq(KycApprovedOrder::getSellerOrderId, req.getSellerOrderId());
        }
        if (ObjectUtils.isNotEmpty(req.getBuyerMemberId())) {
            lambdaQuery.eq(KycApprovedOrder::getBuyerMemberId, req.getBuyerMemberId());
        }
        if (ObjectUtils.isNotEmpty(req.getSellerMemberId())) {
            lambdaQuery.eq(KycApprovedOrder::getSellerMemberId, req.getSellerMemberId());
        }
        if (ObjectUtils.isNotEmpty(req.getRecipientUpi())) {
            lambdaQuery.eq(KycApprovedOrder::getRecipientUpi, req.getRecipientUpi());
        }
        if (ObjectUtils.isNotEmpty(req.getPayerUpi())) {
            lambdaQuery.eq(KycApprovedOrder::getPayerUpi, req.getPayerUpi());
        }
        if (ObjectUtils.isNotEmpty(req.getUtr())) {
            lambdaQuery.eq(KycApprovedOrder::getUtr, req.getUtr());
        }
        if (ObjectUtils.isNotEmpty(req.getTransactionType())) {
            lambdaQuery.eq(KycApprovedOrder::getTransactionType, req.getTransactionType());
        }
        if (ObjectUtils.isNotEmpty(req.getBankCode())) {
            lambdaQuery.eq(KycApprovedOrder::getBankCode, req.getBankCode());
        }
        if (ObjectUtils.isNotEmpty(req.getOrderId())) {
            lambdaQuery.eq(KycApprovedOrder::getOrderId, req.getOrderId());
        }
        lambdaQuery.orderByDesc(KycApprovedOrder::getId);

        Page<KycApprovedOrder> finalPage = page;
        CompletableFuture<KycApprovedOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<KycApprovedOrder>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));

        page = resultFuture.get();
        KycApprovedOrder totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal());

        BigDecimal amountPageTotal = BigDecimal.ZERO;

        List<KycApprovedOrder> records = page.getRecords();
        List<KycApprovedOrderDTO> resultList = new ArrayList<>();
        for (KycApprovedOrder record : records) {
            amountPageTotal = amountPageTotal.add(record.getAmount());
            KycApprovedOrderDTO dto = new KycApprovedOrderDTO();
            BeanUtils.copyProperties(record, dto);
            resultList.add(dto);
        }
        extent.put("amountPageTotal", amountPageTotal);
        return PageUtils.flush(page, resultList, extent);
    }

    /**
     * 根据卖出订单号查询KYC交易订单是否存在
     *
     * @return boolean 如果 KYC交易订单存在，返回true；否则返回false。
     */
    @Override
    public KycApprovedOrder getKycOrderByOrderId(String sellerOrderId) {

        return lambdaQuery()
                .eq(KycApprovedOrder::getSellerOrderId, sellerOrderId)
                .one();
    }

    @Override
    public KycApprovedOrder getKycOrderBuyerByOrderId(String sellerOrderId) {

        return lambdaQuery()
                .eq(KycApprovedOrder::getBuyerOrderId, sellerOrderId)
                .one();
    }
}
