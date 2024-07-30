package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.UsdtAddressDTO;
import org.uu.common.pay.dto.UsdtAddressMemberDTO;
import org.uu.common.pay.dto.UsdtAddressMerchantDTO;
import org.uu.common.pay.req.UsdtAddrPageReq;
import org.uu.wallet.entity.TronAddress;
import org.uu.wallet.mapper.TronAddressMapper;
import org.uu.wallet.service.ITronAddressService;
import org.uu.wallet.service.IUsdtBuyOrderService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 波场用户钱包 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Service
public class TronAddressServiceImpl extends ServiceImpl<TronAddressMapper, TronAddress> implements ITronAddressService {

    @Resource
    private IUsdtBuyOrderService usdtBuyOrderService;

    /**
     * 根据商户id+用户id 查询波场钱包地址
     *
     * @param merchantId
     * @param memberId
     * @return {@link TronAddress }
     */
    @Override
    public TronAddress getTronAddressByMerchanIdtAndUserId(String merchantId, String memberId) {
        return lambdaQuery()
                .eq(TronAddress::getMerchantId, merchantId)
                .eq(TronAddress::getMemberId, memberId)
                .eq(TronAddress::getDeleted, 0)
                .last("LIMIT 1")
                .one();
    }


    /**
     * 根据U地址 查询波场钱包信息
     *
     * @param address
     * @return {@link TronAddress }
     */
    @Override
    public TronAddress getTronAddressByAddress(String address) {
        return lambdaQuery()
                .eq(TronAddress::getAddress, address)
                .eq(TronAddress::getDeleted, 0)
                .last("LIMIT 1")
                .one();
    }


    /**
     * 根据U地址 更新USDT余额和TRX余额
     *
     * @param tronAddress
     */
    @Override
    public boolean updateBalance(TronAddress tronAddress) {

        // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
        LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(TronAddress::getAddress, tronAddress.getAddress())  // 指定更新条件，这里以 address 为条件
                .set(TronAddress::getTrxBalance, tronAddress.getTrxBalance()) // 指定更新字段 trx余额
                .set(TronAddress::getUsdtBalance, tronAddress.getUsdtBalance()) // 指定更新字段 usdt余额
                .set(TronAddress::getUpdateTime, LocalDateTime.now()); // 指定更新字段 更新时间

        // 这里传入的 null 表示不更新实体对象的其他字段
        return update(null, lambdaUpdateWrapper);
    }

    @Override
    public PageReturn<UsdtAddressDTO> addressListPage(UsdtAddrPageReq req) {
        Page<TronAddress> page = new Page<>(req.getPageNo(), req.getPageSize());

        lambdaQuery()
                .orderBy(StringUtils.isNotEmpty(req.getColumn()) && req.getColumn().equals("usdtBalance"),
                        req.isAsc(), TronAddress::getUsdtBalance)
                .orderBy(StringUtils.isNotEmpty(req.getColumn()) && req.getColumn().equals("trxBalance"),
                        req.isAsc(), TronAddress::getTrxBalance)
                .orderBy(StringUtils.isNotEmpty(req.getColumn()) && req.getColumn().equals("orderTotal"),
                        req.isAsc(), TronAddress::getOrderTotal)
                .orderBy(StringUtils.isNotEmpty(req.getColumn()) && req.getColumn().equals("orderSuccessNum"),
                        req.isAsc(), TronAddress::getOrderSuccessNum)
                .orderByDesc(TronAddress::getCreateTime)
                .ge(req.getCreateTimeStart() != null, TronAddress::getCreateTime, req.getCreateTimeStart())
                .le(req.getCreateTimeEnd() != null, TronAddress::getCreateTime, req.getCreateTimeEnd())
                .eq(StringUtils.isNotEmpty(req.getMemberId()), TronAddress::getMemberId, req.getMemberId())
                .eq(StringUtils.isNotEmpty(req.getMerchantId()), TronAddress::getMerchantId, req.getMerchantId())
                .eq(StringUtils.isNotEmpty(req.getMerchantName()), TronAddress::getMerchantName, req.getMerchantName())
                .eq(StringUtils.isNotEmpty(req.getUsdtAddr()), TronAddress::getAddress, req.getUsdtAddr())
                .eq(Objects.nonNull(req.getType()) && req.getType().equals(1), TronAddress::getMerchantId, "uuPay")
                .ne(Objects.nonNull(req.getType()) && req.getType().equals(2), TronAddress::getMerchantId, "uuPay")
                .page(page);

        List<UsdtAddressDTO> list = page.getRecords().stream()
                .map(tronAddress -> UsdtAddressDTO.builder()
                        .orderTotal(tronAddress.getOrderTotal())
                        .createTime(tronAddress.getCreateTime())
                        .usdtBalance(tronAddress.getUsdtBalance())
                        .memberId(tronAddress.getMemberId())
                        .merchantId(tronAddress.getMerchantId())
                        .merchantName(tronAddress.getMerchantName())
                        .trxBalance(tronAddress.getTrxBalance())
                        .usdtAddr(tronAddress.getAddress())
                        .networkProtocol("TRC-20") // 暂时写死TRC-20
                        .orderSuccessNum(tronAddress.getOrderSuccessNum())
                        .build())
                .collect(Collectors.toList());

        return PageUtils.flush(page, list);
    }

    @Override
    public PageReturn<UsdtAddressMemberDTO> addressExportPage(UsdtAddrPageReq req) {
        PageReturn<UsdtAddressDTO> pageReturn = addressListPage(req);
        List<UsdtAddressMemberDTO> dtoList = pageReturn.getList().stream()
                .map(dto -> {
                    UsdtAddressMemberDTO usdtAddressMemberDTO = new UsdtAddressMemberDTO();
                    BeanUtils.copyProperties(dto, usdtAddressMemberDTO);
                    return usdtAddressMemberDTO;
                })
                .collect(Collectors.toList());

        Page<UsdtAddressMemberDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(pageReturn.getTotal());
        return PageUtils.flush(page, dtoList);
    }

    @Override
    public PageReturn<UsdtAddressMerchantDTO> addressMerchantExportPage(UsdtAddrPageReq req) {
        PageReturn<UsdtAddressDTO> pageReturn = addressListPage(req);
        List<UsdtAddressMerchantDTO> dtoList = pageReturn.getList().stream()
                .map(dto -> {
                    UsdtAddressMerchantDTO usdtAddressMerchantDTO = new UsdtAddressMerchantDTO();
                    BeanUtils.copyProperties(dto, usdtAddressMerchantDTO);
                    return usdtAddressMerchantDTO;
                })
                .collect(Collectors.toList());

        Page<UsdtAddressMerchantDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(pageReturn.getTotal());
        return PageUtils.flush(page, dtoList);
    }
}
