/**
 * Copyright 2022 json.cn
 */
package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class RawData implements Serializable {


    /**
     * 交易合约的详细信息
     */
    private List<Contract> contract;

    /**
     *  引用的区块字节
     */
    private String refBlockBytes;

    /**
     * 引用的区块哈希值
     */
    private String refBlockHash;

    /**
     * 交易过期时间
     */
    private long expiration;

    /**
     * 交易的费用上限
     */
    private long feeLimit;

    /**
     * 交易的时间戳
     */
    private long timestamp;

}