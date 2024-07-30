package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.RechargeTronDetailDTO;
import org.uu.common.pay.dto.RechargeTronExportDTO;
import org.uu.common.pay.req.RechargeTronDetailReq;
import org.uu.wallet.entity.RechargeTronDetail;
import org.uu.wallet.mapper.RechargeTronDetailMapper;
import org.uu.wallet.service.IRechargeTronDetailService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 钱包交易记录 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Service
public class RechargeTronDetailServiceImpl extends ServiceImpl<RechargeTronDetailMapper, RechargeTronDetail> implements IRechargeTronDetailService {

    /**
     * 查询两天内交易金额大于等于订单金额 并且未上分的最新订单 USDT
     *
     * @param toAddress
     * @param minRechargeAmount
     * @param toAddress         收款地址
     * @param minRechargeAmount 最低充值金额
     * @return {@link List }<{@link RechargeTronDetail }>
     * /**
     * @return {@link RechargeTronDetail}
     */
    public RechargeTronDetail getLatestPendingOrderWithLock(String toAddress, BigDecimal minRechargeAmount) {
        LambdaQueryWrapper<RechargeTronDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RechargeTronDetail::getAmount, minRechargeAmount)//交易金额大于等于最低限制金额
                .eq(RechargeTronDetail::getOrderId, "0")//未上过分
                .eq(RechargeTronDetail::getSymbol, "USDT")//币种: USDT
                .eq(RechargeTronDetail::getToAddress, toAddress)//钱包地址
                .ge(RechargeTronDetail::getCreateTime, LocalDateTime.now().minusDays(2))//两天内
                .orderByDesc(RechargeTronDetail::getCreateTime)  // 按创建时间降序排序
                .last("LIMIT 1 FOR UPDATE");  // 只查询一条记录并加锁

        return getOne(queryWrapper, false);  // 使用 getOne 方法返回单个对象
    }

    /**
     * 查询两天内交易金额大于等于订单金额 并且未上分的最新订单 TRX
     *
     * @param toAddress
     * @param minRechargeAmount
     * @param toAddress         收款地址
     * @param minRechargeAmount 最低充值金额
     * @return {@link List }<{@link RechargeTronDetail }>
     * /**
     * @return {@link RechargeTronDetail}
     */
    public RechargeTronDetail getLatestPendingOrderWithLockTRX(String toAddress, BigDecimal minRechargeAmount) {
        LambdaQueryWrapper<RechargeTronDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RechargeTronDetail::getAmount, minRechargeAmount)//交易金额大于等于最低限制金额
                .eq(RechargeTronDetail::getOrderId, "0")//未上过分
                .eq(RechargeTronDetail::getSymbol, "TRX")//币种: TRX
                .eq(RechargeTronDetail::getToAddress, toAddress)//钱包地址
                .ge(RechargeTronDetail::getCreateTime, LocalDateTime.now().minusDays(2))//两天内
                .orderByDesc(RechargeTronDetail::getCreateTime)  // 按创建时间降序排序
                .last("LIMIT 1 FOR UPDATE");  // 只查询一条记录并加锁

        return getOne(queryWrapper, false);  // 使用 getOne 方法返回单个对象
    }

    @Override
    public Map<String, RechargeTronDetail> getRechargeTronDetailByTxid(List<String> txids) {
        if (CollectionUtils.isEmpty(txids)) {
            return Collections.emptyMap();
        }

        List<RechargeTronDetail> rechargeTronDetails = lambdaQuery()
                .orderByDesc(RechargeTronDetail::getBetTime)
                .in(RechargeTronDetail::getTxid, txids)
                .list();

        return rechargeTronDetails.stream()
                .collect(Collectors.toMap(RechargeTronDetail::getTxid, Function.identity()));
    }

    @Override
    @Transactional
    public Boolean queryAndFillOrderId(String orderId, String toAddress, BigDecimal amount) {
        if (StringUtils.isNotEmpty(orderId) && StringUtils.isNotEmpty(toAddress) && Objects.nonNull(amount)) {
            RechargeTronDetail rechargeTronDetail = this.baseMapper.selectOneByToAddressAndAmountForUpdate(toAddress, amount);
            if (Objects.isNull(rechargeTronDetail)) {
                return Boolean.FALSE;
            }
            rechargeTronDetail.setOrderId(orderId)
                    .setUpdateTime(LocalDateTime.now());
            return this.updateById(rechargeTronDetail);
        }
        throw new RuntimeException(String.format("参数异常  orderId:%s, toAddress%s, amount:%s", orderId, toAddress, amount));
    }

    @Override
    public PageReturn<RechargeTronDetailDTO> rechargeTronDetailPage(RechargeTronDetailReq req) {
        Page<RechargeTronDetail> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(RechargeTronDetail::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), RechargeTronDetail::getCreateTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), RechargeTronDetail::getCreateTime, req.getCreateTimeEnd())
                .eq(StringUtils.isNotEmpty(req.getOrderId()), RechargeTronDetail::getOrderId, req.getOrderId())
                .eq(StringUtils.isNotEmpty(req.getTxid()), RechargeTronDetail::getTxid, req.getTxid())
                .page(page);

        List<RechargeTronDetailDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    RechargeTronDetailDTO dto = new RechargeTronDetailDTO();
                    BeanUtils.copyProperties(item, dto);
                    dto.setBetTime(Objects.nonNull(item.getBetTime()) ? LocalDateTime.ofInstant(Instant.ofEpochSecond(item.getBetTime() / 1000), ZoneId.systemDefault()) : null);
                    return dto;
                })
                .collect(Collectors.toList());

        return PageUtils.flush(page, resultList);
    }

    @Override
    public PageReturn<RechargeTronExportDTO> rechargeTronDetailPageExport(RechargeTronDetailReq req) {
        PageReturn<RechargeTronDetailDTO> pageReturn = rechargeTronDetailPage(req);
        List<RechargeTronExportDTO> resultList = pageReturn.getList().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    RechargeTronExportDTO dto = new RechargeTronExportDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        Page<RechargeTronExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(pageReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }

   
}
