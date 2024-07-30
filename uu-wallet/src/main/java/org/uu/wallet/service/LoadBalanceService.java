package org.uu.wallet.service;

import java.util.List;

/**
 * 资金归集服务类
 *
 * @author simon
 * @date 2024/07/14
 */
public interface LoadBalanceService {


    /**
     * 指定账户资金归集
     *
     * @param usdtAddresses
     * @return {@link Boolean }
     */
    Boolean collectFundsForAccounts(List<String> usdtAddresses);
}
