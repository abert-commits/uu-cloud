package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.UsdtAddressDTO;
import org.uu.common.pay.dto.UsdtAddressMemberDTO;
import org.uu.common.pay.dto.UsdtAddressMerchantDTO;
import org.uu.common.pay.req.UsdtAddrPageReq;
import org.uu.wallet.entity.TronAddress;

/**
 * <p>
 * 波场用户钱包 服务类
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
public interface ITronAddressService extends IService<TronAddress> {

    /**
     * 根据商户id+用户id 查询波场钱包地址
     *
     * @param merchantId
     * @param memberId
     * @return {@link TronAddress }
     */
    TronAddress getTronAddressByMerchanIdtAndUserId(String merchantId, String memberId);


    /**
     * 根据U地址 查询波场钱包信息
     *
     * @param address
     * @return {@link TronAddress }
     */
    TronAddress getTronAddressByAddress(String address);

    /**
     * 根据U地址 更新USDT余额和TRX余额
     *
     * @param tronAddress
     */
    boolean updateBalance(TronAddress tronAddress);

    PageReturn<UsdtAddressDTO> addressListPage(UsdtAddrPageReq req);

    PageReturn<UsdtAddressMemberDTO> addressExportPage(UsdtAddrPageReq req);

    PageReturn<UsdtAddressMerchantDTO> addressMerchantExportPage(UsdtAddrPageReq req);
}
