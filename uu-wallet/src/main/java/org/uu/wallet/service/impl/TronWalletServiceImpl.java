package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TronWalletAddressDTO;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.TronWallet;
import org.uu.wallet.mapper.TronWalletMapper;
import org.uu.wallet.service.ITronWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 波场钱包地址表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-12
 */
@Service
@RequiredArgsConstructor
public class TronWalletServiceImpl extends ServiceImpl<TronWalletMapper, TronWallet> implements ITronWalletService {

    private final WalletMapStruct walletMapStruct;

    /**
     * 更新钱包余额
     *
     * @param tronAddress
     * @return {@link Object }
     */
    @Override
    public boolean updateBalance(TronWallet tronAddress) {
        //更新波场钱包信息
        LambdaUpdateWrapper<TronWallet> lambdaUpdateWrapperTronWallet = new LambdaUpdateWrapper<>();
        // 指定更新条件，钱包地址
        lambdaUpdateWrapperTronWallet.eq(TronWallet::getAddress, tronAddress.getAddress());
        // 更新TRX余额
        lambdaUpdateWrapperTronWallet.set(TronWallet::getTrxBalance, tronAddress.getTrxBalance());
        // 更新USDT余额
        lambdaUpdateWrapperTronWallet.set(TronWallet::getUsdtBalance, tronAddress.getUsdtBalance());
        // 更新时间
        lambdaUpdateWrapperTronWallet.set(TronWallet::getUpdateTime, LocalDateTime.now());
        // 这里传入的 null 表示不更新实体对象的其他字段
        return update(null, lambdaUpdateWrapperTronWallet);
    }


    /**
     * 根据类型获取钱包地址列表
     *
     * @param type
     * @return {@link TronWallet }
     */
    @Override
    public TronWallet queryAddressByType(Integer type) {
        return lambdaQuery()
                .eq(TronWallet::getWalletType, type)
                .eq(TronWallet::getDeleted, 0)
                .last("LIMIT 1").one();
    }


    /**
     * 根据类型获取钱包地址列表 根据U余额进行排序 加上排他行锁 最多只取一条
     *
     * @param orderAmount
     * @param type
     * @return {@link TronWallet }
     */
    @Transactional
    public TronWallet findOneWalletForPaymentForUpdate(BigDecimal orderAmount, Integer type) {
        LambdaQueryWrapper<TronWallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(TronWallet::getWalletType, type)
                .ge(TronWallet::getUsdtBalance, orderAmount)
                .eq(TronWallet::getDeleted, 0)
                .orderByDesc(TronWallet::getUsdtBalance)
                .last("LIMIT 1 FOR UPDATE");

        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据类型获取钱包地址列表 根据U余额进行排序 加上排他行锁 最多只取一条
     *
     * @param orderAmount
     * @param type
     * @return {@link TronWallet }
     */
    @Transactional
    public TronWallet findOneWalletForPaymentForUpdateTrx(BigDecimal orderAmount, Integer type) {
        LambdaQueryWrapper<TronWallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(TronWallet::getWalletType, type)
                .ge(TronWallet::getTrxBalance, orderAmount)
                .eq(TronWallet::getDeleted, 0)
                .orderByDesc(TronWallet::getTrxBalance)
                .last("LIMIT 1 FOR UPDATE");

        return baseMapper.selectOne(queryWrapper);
    }


    /**
     * 分页查询波场钱包地址列表
     *
     * @param req
     * @return {@link PageReturn }<{@link TronWalletAddressDTO }>
     */
    @Override
    public PageReturn<TronWalletAddressDTO> addressListPage(PageRequest req) {

        Page<TronWallet> page = new Page<>();

        page.setCurrent(req.getPageNo());

        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<TronWallet> lambdaQuery = lambdaQuery();


        lambdaQuery.orderByDesc(TronWallet::getId);

        baseMapper.selectPage(page, lambdaQuery.getWrapper());

        List<TronWallet> records = page.getRecords();
        List<TronWalletAddressDTO> list = walletMapStruct.TronWalletTransform(records);
        return PageUtils.flush(page, list);
    }

}
