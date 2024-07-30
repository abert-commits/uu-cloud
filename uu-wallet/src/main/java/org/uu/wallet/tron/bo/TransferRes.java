package org.uu.wallet.tron.bo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * TRX转账 返回结果
 *
 * @author simon
 * @date 2024/07/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRes implements Serializable {

    /**
     * 转账状态 1: 转账中 2: 转账成功 3: 转账失败
     */
    private Integer Status = 1;

    /**
     * 交易id
     */
    private String txId;
}
