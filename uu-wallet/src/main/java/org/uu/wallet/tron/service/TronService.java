package org.uu.wallet.tron.service;

import org.tron.trident.core.ApiWrapper;
import org.uu.wallet.tron.bo.TransferRes;

import java.math.BigDecimal;

public interface TronService {

    ApiWrapper getApiWrapper(String privateKey);

    /**
     * 实现TRC20代币转账操作
     *
     * @param fromAddress 转出方的地址
     * @param privateKey  转出方的私钥
     * @param toAddress   转入方的地址
     * @param amount      转账金额
     * @return 转账结果（成功时返回交易ID，失败时返回null）
     */
    String transfer(String fromAddress, String privateKey, String toAddress, BigDecimal amount);


    /**
     * TRX转账
     *
     * @param fromAddress 发起转账的地址
     * @param privateKey 发起转账地址的私钥
     * @param toAddress 接收转账的地址
     * @param amount 转账金额
     * @return {@link String } 返回交易的结果
     */
    String transferTrx(String fromAddress, String privateKey, String toAddress, BigDecimal amount);


    /**
     * 实现TRC20代币转账操作，使用中转账户进行转账
     *
     * @param fromAddress 转出方的地址
     * @param destAddr    中转账户地址
     * @param privateKey  中转账户私钥
     * @param toAddress   转入方的地址
     * @param amount      转账金额
     * @return 转账结果（成功时返回交易ID，失败时返回null）
     */
    String transferFrom(String fromAddress, String destAddr, String privateKey, String toAddress, BigDecimal amount);

    BigDecimal queryUSDTBalance(String address);

    /**
     * 授权
     * 授权是指将一个账户的某个代币的转账权限授予另一个账户。
     * 在这个场景中，授权 出款账户 的 USDT 转账权限给 中转账户，使得 中转账户 可以代表 出款账户 进行 USDT 转账操作。
     *
     * @param fromAddress
     * @param privateKey
     * @param toAddress
     * @return {@link String }
     */
    String approve(String fromAddress, String privateKey, String toAddress);


    /**
     * 检查TRON地址是否有效
     *
     * @param address 要检查的TRON地址
     * @return 如果地址有效，返回true；否则返回false
     */
    boolean checkAddress(String address);

}
