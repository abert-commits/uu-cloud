package org.uu.wallet.service;

import org.springframework.stereotype.Service;

/**
 * 处理USDT自动上分
 *
 * @author simon
 * @date 2024/07/04
 */
@Service
public interface UsdtAutoCreditService {

    /**
     * 处理USDT自动上分
     *
     * @param usdtAddress
     * @return {@link Boolean }
     */
    Boolean usdtAutoCredit(String usdtAddress);
}
