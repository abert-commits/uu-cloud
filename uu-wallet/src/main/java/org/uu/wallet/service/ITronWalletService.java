package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.TronWalletAddressDTO;
import org.uu.wallet.entity.TronWallet;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 波场钱包地址表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-12
 */
public interface ITronWalletService extends IService<TronWallet> {

    /**
     * 更新钱包余额
     *
     * @param tronAddress
     * @return {@link Object }
     */
    boolean updateBalance(TronWallet tronAddress);


    /**
     * 根据类型获取钱包地址列表
     *
     * @param type
     * @return {@link List }<{@link TronWallet }>
     */
    TronWallet queryAddressByType(Integer type);


    /**
     * 根据类型获取钱包地址列表 根据U余额进行排序 加上排他行锁 最多只取一条
     *
     * @param orderAmount
     * @return {@link TronWallet }
     */
    TronWallet findOneWalletForPaymentForUpdate(BigDecimal orderAmount, Integer type);


    /**
     * 根据类型获取钱包地址列表 根据U余额进行排序 加上排他行锁 最多只取一条 trx余额
     *
     * @param orderAmount
     * @return {@link TronWallet }
     */
    TronWallet findOneWalletForPaymentForUpdateTrx(BigDecimal orderAmount, Integer type);

    /**
     * 分页查询波场钱包地址列表
     *
     * @param req
     * @return {@link PageReturn }<{@link TronWalletAddressDTO }>
     */
    PageReturn<TronWalletAddressDTO> addressListPage(PageRequest req);
}
