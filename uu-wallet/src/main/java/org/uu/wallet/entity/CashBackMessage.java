package org.uu.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 获取KYC银行交易记录 消息实体
 *
 * @author Simon
 * @date 2024/04/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashBackMessage implements Serializable {

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 商户会员id
     */
    private String merchantMemberId;


}
