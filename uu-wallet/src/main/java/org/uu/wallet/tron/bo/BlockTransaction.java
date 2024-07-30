
package org.uu.wallet.tron.bo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


/**
 * 表示区块链中的一个交易对象，包含了交易的详细信息
 *
 * @author simon
 * @date 2024/07/02
 */
@Setter
@Getter
public class BlockTransaction implements Serializable {


    private static final long serialVersionUID = 9142449601410152650L;


    /**
     * 交易结果列表
     */
    private List<Ret> ret;


    /**
     * 交易的签名列表
     */
    private List<String> signature;

    /**
     * 交易的唯一标识符
     */
    private String txID;

    /**
     * 交易的原始数据，包含交易的详细信息
     */
    private RawData rawData;

    /**
     * 原始数据的十六进制字符串表示
     */
    private String rawDataHex;

}