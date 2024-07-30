package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

@Setter
@Getter
public class TronValue implements Serializable {

    private static final long serialVersionUID = 4635956959925563732L;


    /**
     * 交易金额，表示在这笔交易中转移的代币数量。对于 TRC20 代币（如 USDT）而言，金额通常以最小单位表示，例如 1 USDT = 1000000（如果代币有 6 位小数）
     * 注: 1000000 = 1U
     */
    private BigInteger amount;


    /**
     * 发送方的地址，即发起这笔交易的账户地址。地址以 Base58 格式表示
     */
    private String ownerAddress;


    /**
     * 接收方的地址，即接受这笔交易的账户地址。地址以 Base58 格式表示
     */
    private String toAddress;


    /**
     * 交易数据，通常包含交易的附加信息或参数。对于智能合约调用，data 字段可能包含方法签名和参数的编码值。例如，在 TRC20 代币转账中，data 字段会包含转账方法和参数的编码值
     */
    private String data;


    /**
     * 合约地址，表示执行交易的智能合约地址。对于 TRC20 代币交易，这是代币合约的地址，用于标识该代币合约
     */
    private String contractAddress;
}
