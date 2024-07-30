package org.uu.job.util;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;

public class CryptoWalletGenerator {

    /**
     * 生成钱包地址
     *
     * @return {@link String}
     */
    public static String generateWalletAddress() {
        // 获取比特币主网络的参数
        NetworkParameters params = MainNetParams.get();

        // 创建一个空的钱包
        Wallet wallet = new Wallet(params);

        // 向钱包中添加一个新的 ECKey（即比特币地址）
        wallet.freshReceiveKey();

        // 获取钱包的首个地址
        String btcAddress = wallet.currentReceiveAddress().toString();

        // 输出生成的比特币地址
        return btcAddress;
    }

}
