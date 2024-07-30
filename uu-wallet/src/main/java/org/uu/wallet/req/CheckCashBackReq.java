package org.uu.wallet.req;


import lombok.Data;

import java.io.Serializable;

/**
 * 获取钱包会员信息 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class CheckCashBackReq implements Serializable {

    /**
     * 商户订单号
     */
    private String merchantOrder;


    /**
     * 签名
     */
    private String sign;
}
