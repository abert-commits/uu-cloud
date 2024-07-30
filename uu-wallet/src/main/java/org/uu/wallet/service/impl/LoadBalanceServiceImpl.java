package org.uu.wallet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.wallet.service.LoadBalanceService;
import org.uu.wallet.tron.service.TronBlockService;

import java.util.List;

@Service
public class LoadBalanceServiceImpl implements LoadBalanceService {

    @Autowired
    private TronBlockService tronBlockService;

    /**
     * 指定账户资金归集
     *
     * @param usdtAddresses
     * @return {@link Boolean }
     */
    @Override
    public Boolean collectFundsForAccounts(List<String> usdtAddresses) {
        return tronBlockService.collectFundsForAccounts(usdtAddresses);
    }
}
